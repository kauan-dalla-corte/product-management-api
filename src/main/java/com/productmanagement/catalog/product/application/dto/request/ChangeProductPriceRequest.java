package com.productmanagement.catalog.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeProductPriceRequest(
        @NotBlank String price
) {
}