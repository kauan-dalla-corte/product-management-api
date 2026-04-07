package com.productmanagement.catalog.product.domain.exception;

public class InvalidProductNameException extends DomainException {
    public InvalidProductNameException() {
        super("Invalid product name");
    }
}
