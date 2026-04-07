package com.productmanagement.catalog.product.integration;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductStatus;
import com.productmanagement.catalog.product.support.IntegrationTestSupport;
import com.productmanagement.catalog.product.support.ProductBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductApiIntegrationTest extends IntegrationTestSupport {

    @Test
    public void deveCriarProdutoEPersistirNoBanco() throws
            Exception {

        String body = """
                {
                    "name": "Produto Integração",
                    "price": "19.90",
                    "cost": "5.99"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(successTrue())
                .andExpect(dataPath("name", "PRODUTO INTEGRAÇÃO"))
                .andExpect(dataPath("price", 19.90))
                .andExpect(dataPath("cost", 5.99))
                .andExpect(dataPath("currentStock", 0));

        Product product = productRepository.findAll().getFirst();

        assertEquals(1, productRepository.count());
        assertNotNull(product.getId());
        assertEquals("PRODUTO INTEGRAÇÃO", product.getName().value());
        assertEquals("19.90", product.getPrice().toString());
        assertEquals("5.99", product.getCost().toString());
        assertEquals(0, product.getCurrentStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

    }

    @Test
    public void deveRetornarConflictQuandoCriarProdutoDuplicado() throws
            Exception {
        String body = """
                {
                    "name": "Produto Integração",
                    "price": "19.90",
                    "cost": "5.99"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(successTrue());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_ALREADY_EXISTS"));

        assertEquals(1, productRepository.count());
    }

    @Test
    public void deveAtivarProdutoInativoEPersistirNovoStatus() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().inactive().build());

        mockMvc.perform(patch("/api/v1/products/{id}/activate", product.getId()))
                .andExpect(status().isOk())
                .andExpect(successTrue());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(ProductStatus.ACTIVE, updatedProduct.getStatus());
    }

    @Test
    public void deveDesativarProdutoAtivoSemEstoqueEPersistirNovoStatus() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().build());

        mockMvc.perform(patch("/api/v1/products/{id}/deactivate", product.getId()))
                .andExpect(status().isOk())
                .andExpect(successTrue());

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(ProductStatus.INACTIVE, updatedProduct.getStatus());
    }

    @Test
    public void deveRetornarConflictQuandoTentarDesativarProdutoComEstoque() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().withCurrentStock(5).build());

        mockMvc.perform(patch("/api/v1/products/{id}/deactivate", product.getId()))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("CANNOT_DEACTIVATE_WITH_STOCK"));

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(ProductStatus.ACTIVE, updatedProduct.getStatus());
    }

    @Test
    public void deveRetornarConflictQuandoTentarAtivarProdutoJaAtivo() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().build());

        mockMvc.perform(patch("/api/v1/products/{id}/activate", product.getId()))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_PRODUCT_STATE_TRANSITION"));
    }

    @Test
    public void deveRetornarConflictQuandoTentarDesativarProdutoJaInativo() throws
            Exception {
        Product product = productRepository.saveAndFlush(ProductBuilder.aProduct().inactive().build());

        mockMvc.perform(patch("/api/v1/products/{id}/deactivate", product.getId()))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_PRODUCT_STATE_TRANSITION"));
    }

    @Test
    public void deveRetornarNotFoundQuandoTentarAtivarProdutoInexistente() throws
            Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/activate", 1L))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    public void deveRetornarNotFoundQuandoTentarDesativarProdutoInexistente() throws
            Exception {
        mockMvc.perform(patch("/api/v1/products/{id}/deactivate", 1L))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

}
