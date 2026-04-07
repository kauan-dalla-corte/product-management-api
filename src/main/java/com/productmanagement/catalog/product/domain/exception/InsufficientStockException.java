package com.productmanagement.catalog.product.domain.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException() {
        super("Insufficient stock for the requested operation");
    }
}
