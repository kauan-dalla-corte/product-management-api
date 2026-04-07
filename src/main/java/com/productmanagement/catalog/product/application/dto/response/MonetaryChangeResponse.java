package com.productmanagement.catalog.product.application.dto.response;

import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;

import java.math.BigDecimal;

public record MonetaryChangeResponse(
        Long changeId,
        ProductMonetaryField type,
        BigDecimal newValue,
        BigDecimal oldValue
) {
}
