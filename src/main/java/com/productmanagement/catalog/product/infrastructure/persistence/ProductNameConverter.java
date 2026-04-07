package com.productmanagement.catalog.product.infrastructure.persistence;

import com.productmanagement.catalog.product.domain.model.ProductName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductNameConverter implements AttributeConverter<ProductName, String> {

    @Override
    public String convertToDatabaseColumn(ProductName productName) {
        return productName == null ?
                null :
                productName.value();
    }

    @Override
    public ProductName convertToEntityAttribute(String dbData) {
        return dbData == null ?
                null :
                ProductName.of(dbData);
    }
}
