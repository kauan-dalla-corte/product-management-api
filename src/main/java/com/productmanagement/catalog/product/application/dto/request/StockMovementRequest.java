package com.productmanagement.catalog.product.application.dto.request;

import jakarta.validation.constraints.Min;

public record StockMovementRequest(
        @Min(1) int quantity
) {
}
