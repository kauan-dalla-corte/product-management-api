package com.productmanagement.catalog.product.integration;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.support.IntegrationTestSupport;
import com.productmanagement.catalog.product.support.ProductBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductStockApiIntegrationTest extends IntegrationTestSupport {

    @Test
    public void deveRegistrarEntradaDeEstoqueEPersistirMovimentacao() throws
            Exception {

        Product product = ProductBuilder.aProduct().build();
        productRepository.saveAndFlush(product);

        String body = """
                { "quantity": 20 }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}/stock/inbound", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("quantity", 20))
                .andExpect(dataPath("type", "INBOUND"));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(20, updatedProduct.getCurrentStock());

        ProductStockMovement movement = productStockMovementRepository.findAll().getFirst();
        assertEquals(20, movement.getQuantity());
        assertEquals(StockMovementType.INBOUND, movement.getType());
        assertEquals(product.getId(), updatedProduct.getId());
    }

    @Test
    public void deveRegistrarSaidaDeEstoqueEPersistirMovimentacao() throws
            Exception {

        Product product = ProductBuilder.aProduct().withCurrentStock(20).build();
        productRepository.saveAndFlush(product);

        String body = """
                { "quantity": 10 }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}/stock/outbound", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("quantity", 10))
                .andExpect(dataPath("type", "OUTBOUND"));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(10, updatedProduct.getCurrentStock());

        ProductStockMovement movement = productStockMovementRepository.findAll().getFirst();
        assertEquals(10, movement.getQuantity());
        assertEquals(StockMovementType.OUTBOUND, movement.getType());
        assertEquals(product.getId(), updatedProduct.getId());
    }

    @Test
    public void deveRetornarConflictQuandoRegistrarSaidaComEstoqueInsuficiente() throws
            Exception {

        Product product = ProductBuilder.aProduct().withCurrentStock(5).build();
        productRepository.saveAndFlush(product);

        String body = """
                { "quantity": 10 }
                """;

        mockMvc.perform(patch("/api/v1/products/{productId}/stock/outbound", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INSUFFICIENT_STOCK"));
    }

    @Test
    public void deveRetornarListaDeMovimentacoesPorTipo() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().withName("Produto 1").withCurrentStock(20).build());
        Product anotherProduct = productRepository.saveAndFlush(ProductBuilder.aProduct().withName("Produto 2").withCurrentStock(5).build());

        ProductStockMovement inbound = productStockMovementRepository.saveAndFlush(ProductStockMovement.inbound(product, 8));
        ProductStockMovement outbound = productStockMovementRepository.saveAndFlush(ProductStockMovement.outbound(product, 3));
        productStockMovementRepository.saveAndFlush(ProductStockMovement.inbound(anotherProduct, 99));

        mockMvc.perform(get("/api/v1/products/{productId}/stock/movements", product.getId())
                        .param("type", "INBOUND"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(1))
                .andExpect(dataArrayPath(0, "type", "INBOUND"))
                .andExpect(dataArrayPath(0, "quantity", 8));

        mockMvc.perform(get("/api/v1/products/{productId}/stock/movements", product.getId()))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(2))
                .andExpect(dataArrayPath(0, "type", outbound.getType().name()))
                .andExpect(dataArrayPath(0, "quantity", outbound.getQuantity()))
                .andExpect(dataArrayPath(1, "type", inbound.getType().name()))
                .andExpect(dataArrayPath(1, "quantity", inbound.getQuantity()));
    }
}
