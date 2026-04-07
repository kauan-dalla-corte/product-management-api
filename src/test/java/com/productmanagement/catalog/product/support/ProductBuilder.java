package com.productmanagement.catalog.product.support;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.shared.valueobject.Money;

import java.util.function.Consumer;

public class ProductBuilder {

    private String name = "ProdutoTeste";

    private Money price = Money.of("9.99");

    private Money cost = Money.of("3.99");

    private int currentStock = 0;

    private Consumer<Product> afterBuild = p -> {
    };

    public static ProductBuilder aProduct() {
        return new ProductBuilder();
    }

    public ProductBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder withPrice(String price) {
        this.price = Money.of(price);
        return this;
    }

    public ProductBuilder withCost(String cost) {
        this.cost = Money.of(cost);
        return this;
    }

    public ProductBuilder inactive() {
        this.afterBuild = p -> p.deactivate();
        return this;
    }

    public ProductBuilder withCurrentStock(int currentStock) {
        this.currentStock = currentStock;
        return this;
    }

    public Product build() {
        Product product = new Product(ProductName.of(name), price, cost);
        afterBuild.accept(product);

        if (currentStock > 0) {
            product.inbound(currentStock);
        }
        return product;

    }

}
