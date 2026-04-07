package com.productmanagement.catalog.product.infrastructure.web.response;

public enum ErrorCode {
    // Regras de negócio
    PRODUCT_NOT_FOUND,
    PRODUCT_ALREADY_EXISTS,
    INVALID_PRODUCT_NAME,
    INVALID_VALUE,
    INVALID_QUANTITY,

    // Regras de preço
    PRICE_BELOW_COST,
    COST_UPPER_PRICE,
    SAME_PRICE,
    SAME_COST,

    // Regras de estado
    INVALID_PRODUCT_STATE_TRANSITION,
    CANNOT_DEACTIVATE_WITH_STOCK,
    INSUFFICIENT_STOCK,

    // Outros erros
    INTERNAL_SERVER_ERROR,
    VALIDATION_ERROR,
    INVALID_REQUEST_BODY,
    OPTIMISTIC_LOCK_FAILURE

}
