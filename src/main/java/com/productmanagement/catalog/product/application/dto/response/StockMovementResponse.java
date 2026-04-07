package com.productmanagement.catalog.product.application.dto.response;

import com.productmanagement.catalog.product.domain.model.StockMovementType;

import java.time.Instant;

public record StockMovementResponse(
        int quantity,
        StockMovementType type,
        Instant createdAt
) {
}
