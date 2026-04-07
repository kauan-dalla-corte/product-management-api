package com.productmanagement.catalog.product.domain.model;

import com.productmanagement.catalog.product.domain.exception.CostUpperPriceException;
import com.productmanagement.catalog.product.domain.exception.InsufficientStockException;
import com.productmanagement.catalog.product.domain.exception.InvalidQuantityException;
import com.productmanagement.catalog.product.domain.exception.PriceBelowCostException;
import com.productmanagement.catalog.product.domain.exception.ProductHasStockException;
import com.productmanagement.catalog.product.domain.exception.SameCostException;
import com.productmanagement.catalog.product.domain.exception.SamePriceException;
import com.productmanagement.shared.exception.InvalidMonetaryValueException;
import com.productmanagement.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private ProductName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cost"))
    private Money cost;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Money price;

    @Column(name = "current_stock", nullable = false)
    private int currentStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public Product(ProductName name, Money price, Money cost) {
        if (price.equals(Money.zero())) {
            throw new InvalidMonetaryValueException();
        }
        validatePriceBelowCost(price, cost);

        this.name = name;
        this.price = price;
        this.cost = cost;
        this.currentStock = 0;
        this.createdAt = Instant.now();
    }

    public ProductStockMovement inbound(int quantity) {
        status.ensureModifiable();

        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }

        this.currentStock += quantity;
        return ProductStockMovement.inbound(this, quantity);
    }

    public ProductStockMovement outbound(int quantity) {
        status.ensureModifiable();
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }

        if (quantity > currentStock) {
            throw new InsufficientStockException();
        }

        this.currentStock -= quantity;
        return ProductStockMovement.outbound(this, quantity);
    }

    public void activate() {
        this.status = status.activate();
    }

    public void deactivate() {
        if (this.currentStock > 0) {
            throw new ProductHasStockException();
        }
        this.status = status.deactivate();
    }

    private void validatePriceBelowCost(Money price, Money cost) {
        if (price.isLessThan(cost)) {
            throw new PriceBelowCostException();
        }
    }

    private void validateCostUpperPrice(Money cost) {
        if (cost.isGreaterThan(this.price)) {
            throw new CostUpperPriceException();
        }
    }

    public ProductMonetaryChange changePrice(Money newPrice) {
        status.ensureModifiable();
        if (newPrice.equals(Money.zero()))
            throw new InvalidMonetaryValueException();


        if (this.price.equals(newPrice))
            throw new SamePriceException();


        validatePriceBelowCost(newPrice, this.cost);
        Money oldPrice = this.price;
        this.price = newPrice;
        return ProductMonetaryChange.priceUpdate(this, newPrice, oldPrice);
    }

    public ProductMonetaryChange changeCost(Money newCost) {
        status.ensureModifiable();
        if (this.cost.equals(newCost)) {
            throw new SameCostException();
        }
        validateCostUpperPrice(newCost);
        Money oldCost = this.cost;
        this.cost = newCost;
        return ProductMonetaryChange.costUpdate(this, newCost, oldCost);
    }
}
