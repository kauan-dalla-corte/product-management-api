package com.productmanagement.catalog.product.application.mapper;

import com.productmanagement.catalog.product.application.dto.response.StockMovementResponse;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;

import java.util.List;

public final class ProductStockMapper {
    private ProductStockMapper() {
    }

    public static StockMovementResponse toProductStockMovementResponse(
            ProductStockMovement productStockMovement) {
        return new StockMovementResponse(productStockMovement.getQuantity(),
                productStockMovement.getType(),
                productStockMovement.getCreatedAt());
    }

    public static List<StockMovementResponse> toProductStockMovementResponseList(List<ProductStockMovement> movements) {
        return movements.stream().map(ProductStockMapper::toProductStockMovementResponse).toList();
    }

}
