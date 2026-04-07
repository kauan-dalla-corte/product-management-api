package com.productmanagement.catalog.product.domain.exception;

public class ProductHasStockException extends DomainException {
    public ProductHasStockException() {
        super("Operation not allowed because the product still has stock");
    }
}
