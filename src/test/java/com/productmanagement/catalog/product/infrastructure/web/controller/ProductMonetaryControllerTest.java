package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.service.ProductMonetaryChangeService;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.infrastructure.web.exception.GlobalExceptionHandler;
import com.productmanagement.catalog.product.support.ControllerTestSupport;
import com.productmanagement.catalog.product.support.ProductMonetaryChangeBuilder;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductMonetaryController.class)
@Import(GlobalExceptionHandler.class)
public class ProductMonetaryControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductMonetaryChangeService productMonetaryChangeService;

    @Test
    void deveAlterarPrecoDoProduto() throws
            Exception {
        ProductMonetaryChange change = ProductMonetaryChangeBuilder.aPriceChange()
                .withNewValue(Money.of("19.99"))
                .withOldValue(Money.of("10.99"))
                .build();

        when(productMonetaryChangeService.changePrice(eq(999L), any(Money.class)))
                .thenReturn(change);

        String body = """
                { "price": "19.99" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("type", "PRICE"))
                .andExpect(dataPath("newValue", 19.99))
                .andExpect(dataPath("oldValue", 10.99));

    }

    @Test
    void deveRetornarBadRequestQuandoRequestDePrecoForInvalido() throws
            Exception {
        String body = """
                { "price": "" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarBadRequestQuandoFormatoDePrecoForInvalido() throws
            Exception {
        String body = """
                { "price": "abc" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_VALUE"));
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistirAoAlterarPreco() throws
            Exception {
        when(productMonetaryChangeService.changePrice(any(), any()))
                .thenThrow(new ProductNotFoundException());

        String body = """
                { "price": "10.00" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveAlterarCustoDoProduto() throws
            Exception {
        ProductMonetaryChange change = ProductMonetaryChangeBuilder.aCostChange()
                .withNewValue(Money.of("5.00"))
                .withOldValue(Money.of("3.99"))
                .build();

        when(productMonetaryChangeService.changeCost(eq(999L), any(Money.class)))
                .thenReturn(change);

        String body = """
                { "cost": "5.00" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("type", "COST"))
                .andExpect(dataPath("newValue", 5.00))
                .andExpect(dataPath("oldValue", 3.99));

    }

    @Test
    void deveRetornarBadRequestQuandoRequestDeCustoForInvalido() throws
            Exception {
        String body = """
                { "cost": "" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarBadRequestQuandoFormatoDeCustoForInvalido() throws
            Exception {
        String body = """
                { "cost": "abc" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_VALUE"));
    }

    @Test
    void deveRetornarNotFoundQuandoProdutoNaoExistirAoAlterarCusto() throws
            Exception {
        when(productMonetaryChangeService.changeCost(any(), any()))
                .thenThrow(new ProductNotFoundException());

        String body = """
                { "cost": "10.00" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/cost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarHistoricoDePreco() throws
            Exception {
        List<ProductMonetaryChange> changes =
                List.of(ProductMonetaryChangeBuilder.aPriceChange().build());

        when(productMonetaryChangeService.priceChanges(999L))
                .thenReturn(changes);

        mockMvc.perform(get("/api/v1/products/999/monetary/price/history"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(1))
                .andExpect(dataArrayPath(0, "type", "PRICE"))
                .andExpect(dataArrayPath(0, "oldValue", 9.99))
                .andExpect(dataArrayPath(0, "newValue", 19.99));

    }

    @Test
    void deveRetornarHistoricoDeCusto() throws
            Exception {
        List<ProductMonetaryChange> changes =
                List.of(ProductMonetaryChangeBuilder.aCostChange().build());

        when(productMonetaryChangeService.costChanges(999L))
                .thenReturn(changes);

        mockMvc.perform(get("/api/v1/products/999/monetary/cost/history"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(1))
                .andExpect(dataArrayPath(0, "type", "COST"))
                .andExpect(dataArrayPath(0, "oldValue", 3.99))
                .andExpect(dataArrayPath(0, "newValue", 5.99));

    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverHistoricoDeCusto() throws
            Exception {
        when(productMonetaryChangeService.costChanges(999L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/999/monetary/cost/history"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(0));
    }

    @Test
    void deveRetornarNotFoundQuandoBuscarHistoricoDeCustoDeProdutoInexistente() throws
            Exception {
        when(productMonetaryChangeService.costChanges(any()))
                .thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/v1/products/999/monetary/cost/history"))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarBadRequestQuandoProductIdForInvalidoNoHistoricoDeCusto() throws
            Exception {
        mockMvc.perform(get("/api/v1/products/0/monetary/cost/history"))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverHistoricoDePreco() throws
            Exception {
        when(productMonetaryChangeService.priceChanges(999L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products/999/monetary/price/history"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataArraySize(0));
    }

    @Test
    void deveRetornarNotFoundQuandoBuscarHistoricoDePrecoDeProdutoInexistente() throws
            Exception {
        when(productMonetaryChangeService.priceChanges(any()))
                .thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/v1/products/999/monetary/price/history"))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    void deveRetornarBadRequestQuandoProductIdForInvalidoNoHistoricoDePreco() throws
            Exception {
        mockMvc.perform(get("/api/v1/products/0/monetary/price/history"))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    void deveRetornarErroInternoQuandoErroInesperadoOcorrer() throws
            Exception {
        when(productMonetaryChangeService.changePrice(any(), any()))
                .thenThrow(new RuntimeException());

        String body = """
                { "price": "10.00" }
                """;

        mockMvc.perform(patch("/api/v1/products/999/monetary/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(successFalse())
                .andExpect(errorCode("INTERNAL_SERVER_ERROR"));
    }
}
