package com.productmanagement.catalog.product.domain.exception;

public class InvalidQuantityException extends DomainException {
    public InvalidQuantityException() {
        super("Quantity must be greater than zero");
    }
}
