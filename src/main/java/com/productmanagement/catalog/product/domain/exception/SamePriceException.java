package com.productmanagement.catalog.product.domain.exception;

public class SamePriceException extends DomainException {
    public SamePriceException() {
        super("The new price must be different from the current price");
    }
}
