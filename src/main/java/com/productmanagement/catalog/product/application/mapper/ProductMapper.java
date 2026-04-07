package com.productmanagement.catalog.product.application.mapper;

import com.productmanagement.catalog.product.application.dto.request.CreateProductRequest;
import com.productmanagement.catalog.product.application.dto.response.ProductResponse;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.shared.valueobject.Money;

import java.util.List;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(),
                product.getName().value(),
                product.getPrice().getValue(),
                product.getCost().getValue(),
                product.getCurrentStock(),
                product.getStatus());
    }

    public static List<ProductResponse> toProductResponseList(List<Product> productList) {
        return productList.stream().map(ProductMapper::toProductResponse).toList();
    }

    public static Product toDomain(CreateProductRequest createProductRequest) {
        return new Product(ProductName.of(createProductRequest.name()),
                Money.of(createProductRequest.price()),
                Money.of(createProductRequest.cost()));
    }

    public static Money toMoney(String value) {
        return Money.of(value);
    }

}
