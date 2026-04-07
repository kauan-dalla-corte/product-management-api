package com.productmanagement.catalog.product.domain.exception;

public class PriceBelowCostException extends DomainException {
    public PriceBelowCostException() {
        super("Price cannot be lower than cost");
    }
}
