package com.productmanagement.catalog.product.domain.exception;

public class InvalidProductStateTransitionException extends DomainException {
    public InvalidProductStateTransitionException() {
        super("This product status change is not allowed");
    }
}
