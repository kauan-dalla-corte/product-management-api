package com.productmanagement.catalog.product.infrastructure.web.response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ApiMeta(String timestamp) {
    public static ApiMeta now() {
        return new ApiMeta(OffsetDateTime.now(ZoneOffset.UTC).toString());
    }
}
