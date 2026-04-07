package com.productmanagement.catalog.product.domain.exception;

public class CostUpperPriceException extends DomainException {
    public CostUpperPriceException() {
        super("Cost cannot be greater than price");
    }
}
