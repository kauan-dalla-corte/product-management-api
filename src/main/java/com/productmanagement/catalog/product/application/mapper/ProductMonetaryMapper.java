package com.productmanagement.catalog.product.application.mapper;

import com.productmanagement.catalog.product.application.dto.response.MonetaryChangeResponse;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;

import java.util.List;

public final class ProductMonetaryMapper {
    private ProductMonetaryMapper() {
    }

    public static MonetaryChangeResponse toProductMonetaryChangeResponse(
            ProductMonetaryChange productMonetaryChange) {
        return new MonetaryChangeResponse(productMonetaryChange.getId(),
                productMonetaryChange.getMonetaryField(),
                productMonetaryChange.getNewValue().getValue(),
                productMonetaryChange.getOldValue().getValue(),
                productMonetaryChange.getCreatedAt());

    }

    public static List<MonetaryChangeResponse> toProductMonetaryChangeResponseList(List<ProductMonetaryChange> changes) {
        return changes.stream().map(ProductMonetaryMapper::toProductMonetaryChangeResponse).toList();
    }

}
