package com.productmanagement.catalog.product.infrastructure.web.response;

public record ApiError(ErrorCode code, String message, Object details) {
}
