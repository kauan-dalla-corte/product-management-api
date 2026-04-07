package com.productmanagement.catalog.product.support;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.shared.valueobject.Money;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

public class ProductMonetaryChangeBuilder {

    private Long id = 1L;

    private Product product = ProductBuilder.aProduct().build();

    private Money oldValue = Money.of("9.99");

    private Money newValue = Money.of("19.99");

    private Instant createdAt = Instant.now();

    private MonetaryChangeFactory factory = ProductMonetaryChange::priceUpdate;

    public static ProductMonetaryChangeBuilder aPriceChange() {
        return new ProductMonetaryChangeBuilder()
                .withFactory(ProductMonetaryChange::priceUpdate)
                .withOldValue(Money.of("9.99"))
                .withNewValue(Money.of("19.99"));
    }

    public static ProductMonetaryChangeBuilder aCostChange() {
        return new ProductMonetaryChangeBuilder()
                .withFactory(ProductMonetaryChange::costUpdate)
                .withOldValue(Money.of("3.99"))
                .withNewValue(Money.of("5.99"));
    }

    public ProductMonetaryChangeBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProductMonetaryChangeBuilder withProduct(Product product) {
        this.product = product;
        return this;
    }

    public ProductMonetaryChangeBuilder withOldValue(Money oldValue) {
        this.oldValue = oldValue;
        return this;
    }

    public ProductMonetaryChangeBuilder withNewValue(Money newValue) {
        this.newValue = newValue;
        return this;
    }

    public ProductMonetaryChangeBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    private ProductMonetaryChangeBuilder withFactory(MonetaryChangeFactory factory) {
        this.factory = factory;
        return this;
    }

    public ProductMonetaryChange build() {
        ProductMonetaryChange change = factory.create(product, newValue, oldValue);

        ReflectionTestUtils.setField(change, "id", id);
        ReflectionTestUtils.setField(change, "createdAt", createdAt);

        return change;
    }

    @FunctionalInterface
    private interface MonetaryChangeFactory {
        ProductMonetaryChange create(Product product, Money newValue, Money oldValue);
    }
}