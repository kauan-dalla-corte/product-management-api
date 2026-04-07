package com.productmanagement.catalog.product.application.dto.response;

import com.productmanagement.catalog.product.domain.model.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        BigDecimal cost,
        int currentStock,
        ProductStatus status
) {
}

