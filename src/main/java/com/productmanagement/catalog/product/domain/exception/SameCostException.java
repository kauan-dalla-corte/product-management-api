package com.productmanagement.catalog.product.domain.exception;

public class SameCostException extends DomainException {
    public SameCostException() {
        super("The new cost must be different from the current cost");
    }
}
