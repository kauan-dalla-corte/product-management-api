package com.productmanagement.catalog.product.domain.repository;

import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductStockMovementRepository extends JpaRepository<ProductStockMovement, Long> {

    @Query("""
            SELECT m FROM ProductStockMovement m
            WHERE m.product.id = :productId
              AND (:type IS NULL OR m.type = :type)
            ORDER BY m.createdAt DESC
            """)
    List<ProductStockMovement> findAllByProductIdAndOptionalType(
            @Param("productId") Long productId,
            @Param("type") StockMovementType type
    );

}
