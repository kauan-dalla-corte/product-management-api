package com.productmanagement.catalog.product.domain.exception;

public class ProductAlreadyExistsException extends DomainException {
    public ProductAlreadyExistsException() {
        super("A product with this name already exists");
    }
}
