package com.productmanagement.catalog.product.domain.model;

import com.productmanagement.catalog.product.domain.exception.InvalidProductNameException;

import java.util.Locale;
import java.util.regex.Pattern;

public record ProductName(String value) {
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("^(?=.{4,60}$)[\\p{L}\\p{N}]+(?:[\\p{L}\\p{N}\\s().,\\-]*[\\p{L}\\p{N}])?$");

    public ProductName(String value) {
        String normalizedValue = normalize(value);
        validate(normalizedValue);
        this.value = normalizedValue;
    }

    public static ProductName of(String value) {
        return new ProductName(value);
    }

    private static void validate(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidProductNameException();
        }

        if (!PRODUCT_NAME_PATTERN.matcher(value).matches()) {
            throw new InvalidProductNameException();
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

}

