package com.productmanagement.catalog.product.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(boolean success, T data, ApiError error, ApiMeta meta) {
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data, null, ApiMeta.now());
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(true, null, null, ApiMeta.now());
    }

    public static ApiResult<Void> fail(ErrorCode code, String message) {
        return new ApiResult<>(false, null, new ApiError(code, message, null), ApiMeta.now());
    }

    public static ApiResult<Void> fail(ErrorCode code, String message, Object details) {
        return new ApiResult<>(false, null, new ApiError(code, message, details), ApiMeta.now());
    }
}
