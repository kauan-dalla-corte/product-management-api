package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.service.ProductStockMovementService;
import com.productmanagement.catalog.product.domain.exception.InsufficientStockException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.infrastructure.web.exception.GlobalExceptionHandler;
import com.productmanagement.catalog.product.support.ControllerTestSupport;
import com.productmanagement.catalog.product.support.ProductBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductStockController.class)
@Import(GlobalExceptionHandler.class)
public class ProductStockControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductStockMovementService productStockMovementService;

    @Test
    void deveRegistrarEntradaDeEstoque() throws
            Exception {
        Product product = ProductBuilder.aProduct().build();
        ProductStockMovement movement = ProductStockMovement.inbound(product, 5);

        when(productStockMovementService.stockInbound(1L, 5)).thenReturn(movement);

        String body = """
                { "quantity": 5 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("quantity", 5))
                .andExpect(dataPath("type", "INBOUND"))
        ;

        verify(productStockMovementService).stockInbound(1L, 5);
    }

    @Test
    void deveRegistrarSaidaDeEstoque() throws
            Exception {
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();
        ProductStockMovement movement = ProductStockMovement.outbound(product, 3);

        when(productStockMovementService.stockOutbound(1L, 3)).thenReturn(movement);

        String body = """
                { "quantity": 3 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("quantity", 3))
                .andExpect(dataPath("type", "OUTBOUND"));

        verify(productStockMovementService).stockOutbound(1L, 3);
    }

    @Test
    void deveRetornarBadRequestQuandoQuantidadeForInvalidaNaEntrada() throws
            Exception {
        String body = """
                { "quantity": 0 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarBadRequestQuandoQuantidadeForInvalidaNaSaida() throws
            Exception {
        String body = """
                { "quantity": 0 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistirNaEntrada() throws
            Exception {
        when(productStockMovementService.stockInbound(anyLong(), anyInt()))
                .thenThrow(new ProductNotFoundException());

        String body = """
                { "quantity": 5 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistirNaSaida() throws
            Exception {
        when(productStockMovementService.stockOutbound(anyLong(), anyInt()))
                .thenThrow(new ProductNotFoundException());

        String body = """
                { "quantity": 5 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarConflictQuandoEstoqueForInsuficienteNaSaida() throws
            Exception {
        when(productStockMovementService.stockOutbound(anyLong(), anyInt()))
                .thenThrow(new InsufficientStockException());

        String body = """
                { "quantity": 999 }
                """;

        mockMvc.perform(patch("/api/v1/products/1/stock/outbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INSUFFICIENT_STOCK"));
    }

    @Test
    void deveRetornarMovimentacoesDeEstoqueSemFiltro() throws
            Exception {
        Product product = ProductBuilder.aProduct().build();
        ProductStockMovement inbound = ProductStockMovement.inbound(product, 7);
        ProductStockMovement outbound = ProductStockMovement.outbound(product, 2);

        when(productStockMovementService.stockMovements(1L, null)).thenReturn(List.of(inbound, outbound));

        mockMvc.perform(get("/api/v1/products/1/stock/movements"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(2))
                .andExpect(dataArrayPath(0, "type", "INBOUND"))
                .andExpect(dataArrayPath(0, "quantity", 7))
                .andExpect(dataArrayPath(1, "type", "OUTBOUND"))
                .andExpect(dataArrayPath(1, "quantity", 2));
        ;
        ;

        verify(productStockMovementService).stockMovements(1L, null);
    }

    @Test
    void deveRetornarBadRequestQuandoFiltroPorTipoForInvalido() throws
            Exception {

        mockMvc.perform(get("/api/v1/products/1/stock/movements")
                        .param("type", "INVALIDTYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));

    }

    @Test
    void deveRetornarMovimentacoesDeEstoqueComFiltroPorTipo() throws
            Exception {
        Product product = ProductBuilder.aProduct().build();
        ProductStockMovement inbound = ProductStockMovement.inbound(product, 7);

        when(productStockMovementService.stockMovements(1L, StockMovementType.INBOUND))
                .thenReturn(List.of(inbound));

        mockMvc.perform(get("/api/v1/products/1/stock/movements")
                        .param("type", "INBOUND"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("[0]type", "INBOUND"));

        verify(productStockMovementService).stockMovements(1L, StockMovementType.INBOUND);
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistirAoListarMovimentacoes() throws
            Exception {
        when(productStockMovementService.stockMovements(eq(1L), eq(null)))
                .thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/v1/products/1/stock/movements"))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarBadRequestQuandoProductIdForInvalidoAoListarMovimentacoes() throws
            Exception {
        mockMvc.perform(get("/api/v1/products/0/stock/movements"))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }
}
