package com.productmanagement.catalog.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeProductCostRequest(
        @NotBlank String cost
) {
}
