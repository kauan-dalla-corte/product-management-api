package com.productmanagement.catalog.product.integration;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import com.productmanagement.catalog.product.support.IntegrationTestSupport;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.catalog.product.support.ProductMonetaryChangeBuilder;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductPricingApiIntegrationTest extends IntegrationTestSupport {

    @Test
    public void deveAlterarPrecoEPersistirHistoricoMonetario() throws
            Exception {

        Product product = productRepository.save(ProductBuilder.aProduct().withPrice("15.98").withCost("12.29").build());

        String body = """
                { "price": "18.79" }
                """;

        mockMvc.perform(patch("/api/v1/products/{id}/monetary/price", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("newValue", 18.79))
                .andExpect(dataPath("oldValue", 15.98))
                .andExpect(dataPath("type", "PRICE"));

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("18.79", updated.getPrice().toString());

        ProductMonetaryChange productMonetaryChange = productMonetaryChangeRepository.findAll().getFirst();
        assertEquals(1, productMonetaryChangeRepository.count());
        assertEquals("18.79", productMonetaryChange.getNewValue().toString());
        assertEquals("15.98", productMonetaryChange.getOldValue().toString());
        assertEquals(ProductMonetaryField.PRICE, productMonetaryChange.getMonetaryField());

    }

    @Test
    public void deveAlterarCustoEPersistirHistoricoMonetario() throws
            Exception {

        Product product = productRepository.save(ProductBuilder.aProduct().withPrice("15.98").withCost("12.29").build());

        String body = """
                { "cost": "9.98" }
                """;

        mockMvc.perform(patch("/api/v1/products/{id}/monetary/monetary/cost", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("newValue", 9.98))
                .andExpect(dataPath("oldValue", 12.29))
                .andExpect(dataPath("type", "COST"));

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("9.98", updated.getCost().toString());

        ProductMonetaryChange productMonetaryChange = productMonetaryChangeRepository.findAll().getFirst();
        assertEquals(1, productMonetaryChangeRepository.count());
        assertEquals("9.98", productMonetaryChange.getNewValue().toString());
        assertEquals("12.29", productMonetaryChange.getOldValue().toString());
        assertEquals(ProductMonetaryField.COST, productMonetaryChange.getMonetaryField());

    }

    @Test
    public void deveRetornarHistoricoDePreco() throws
            Exception {

        Product product = ProductBuilder.aProduct().build();
        productRepository.save(product);
        List<ProductMonetaryChange> monetaryChangeList =
                List.of(ProductMonetaryChangeBuilder.aPriceChange().withProduct(product).withCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS))
                                .withNewValue(Money.of("19.90")).withId(null).build(),
                        ProductMonetaryChangeBuilder.aPriceChange().withProduct(product).withNewValue(Money.of("17.99")).withId(null).build(),
                        ProductMonetaryChangeBuilder.aCostChange().withProduct(product).withId(null).build());
        productMonetaryChangeRepository.saveAll(monetaryChangeList);

        mockMvc.perform(get("/api/v1/products/{id}/monetary/price/history", product.getId()))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(2))
                .andExpect(dataArrayPath(0, "type", "PRICE"))
                .andExpect(dataArrayPath(0, "newValue", 17.99))
                .andExpect(dataArrayPath(1, "type", "PRICE"))
                .andExpect(dataArrayPath(1, "newValue", 19.90));
    }
}
