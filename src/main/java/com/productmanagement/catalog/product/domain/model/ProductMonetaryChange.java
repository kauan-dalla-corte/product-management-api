package com.productmanagement.catalog.product.domain.model;

import com.productmanagement.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_product_monetary_change")
public class ProductMonetaryChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "new_value", nullable = false))
    private Money newValue;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "old_value", nullable = false))
    private Money oldValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "monetary_field", nullable = false)
    private ProductMonetaryField monetaryField;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ProductMonetaryChange(Product product, Money newValue, Money oldValue, ProductMonetaryField monetaryField) {
        this.product = product;
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.monetaryField = monetaryField;
        createdAt = Instant.now();
    }

    public static ProductMonetaryChange priceUpdate(Product product, Money newValue, Money oldValue) {
        return new ProductMonetaryChange(product, newValue, oldValue, ProductMonetaryField.PRICE);
    }

    public static ProductMonetaryChange costUpdate(Product product, Money newValue, Money oldValue) {
        return new ProductMonetaryChange(product, newValue, oldValue, ProductMonetaryField.COST);
    }

}
