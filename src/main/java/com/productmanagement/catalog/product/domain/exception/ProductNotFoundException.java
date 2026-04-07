package com.productmanagement.catalog.product.domain.exception;

public class ProductNotFoundException extends DomainException {
    public ProductNotFoundException() {
        super("Product not found");
    }
}
