package com.productmanagement.catalog.product.domain.model;

import jakarta.persistence.Column;
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
@Table(name = "tb_product_stock_movement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ProductStockMovement(Product product, StockMovementType type, int quantity) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.createdAt = Instant.now();
    }

    public static ProductStockMovement inbound(Product product, int quantity) {
        return new ProductStockMovement(product, StockMovementType.INBOUND, quantity);
    }

    public static ProductStockMovement outbound(Product product, int quantity) {
        return new ProductStockMovement(product, StockMovementType.OUTBOUND, quantity);
    }


}
