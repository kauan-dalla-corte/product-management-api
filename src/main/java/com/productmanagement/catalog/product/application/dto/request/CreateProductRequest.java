package com.productmanagement.catalog.product.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @NotBlank @Size(min = 4, max = 60) String name,
        @NotBlank String price,
        @NotBlank String cost

) {
}
