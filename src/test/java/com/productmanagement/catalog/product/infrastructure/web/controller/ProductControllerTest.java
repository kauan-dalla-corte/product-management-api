package com.productmanagement.catalog.product.infrastructure.web.controller;

import com.productmanagement.catalog.product.application.dto.request.CreateProductRequest;
import com.productmanagement.catalog.product.application.service.ProductService;
import com.productmanagement.catalog.product.domain.exception.InvalidProductStateTransitionException;
import com.productmanagement.catalog.product.domain.exception.ProductAlreadyExistsException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.infrastructure.web.exception.GlobalExceptionHandler;
import com.productmanagement.catalog.product.support.ControllerTestSupport;
import com.productmanagement.catalog.product.support.ProductBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
public class ProductControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    public void deveListarTodosOsProdutos() throws
            Exception {
        Product product1 = ProductBuilder.aProduct().withName("Produto1").build();
        Product product2 = ProductBuilder.aProduct().withName("Produto2").build();

        when(productService.findAll()).thenReturn(List.of(product1, product2));
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("[0]name", "PRODUTO1"))
                .andExpect(dataPath("[1]name", "PRODUTO2"));
    }

    @Test
    public void deveRetornarListaVaziaQuandoNaoExistirProdutos() throws
            Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(isEmptyData());
    }

    @Test
    public void deveBuscarProdutoPorId() throws
            Exception {
        when(productService.getProductById(1L)).thenReturn(ProductBuilder.aProduct().build());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(successTrue())
                .andExpect(dataPath("name", "PRODUTOTESTE"))
                .andExpect(dataPath("price", "9.99"))
                .andExpect(dataPath("cost", "3.99"))
                .andExpect(dataPath("status", "ACTIVE"));

        verify(productService).getProductById(1L);
    }

    @Test
    public void deveRetornarNotFoundQuandoBuscarProdutoComIdInexistente() throws
            Exception {
        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"))
                .andExpect(errorMessageExists());
    }

    @Test
    public void deveRetornarBadRequestQuandoIdForInvalido() throws
            Exception {
        mockMvc.perform(get("/api/v1/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(errorCode("VALIDATION_ERROR"));
    }

    @Test
    public void deveCriarProdutoComSucesso() throws
            Exception {
        Product product = ProductBuilder.aProduct().build();

        when(productService.create(any(CreateProductRequest.class))).thenReturn(product);

        String requestBody = """
                {
                  "name": "ProdutoTeste",
                  "price": "9.99",
                  "cost": "3.99"
                }
                """;
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(successTrue())
                .andExpect(dataPath("name", "PRODUTOTESTE"))
                .andExpect(dataPath("price", "9.99"))
                .andExpect(dataPath("cost", "3.99"))
                .andExpect(dataPath("status", "ACTIVE"));
        ;
    }

    @Test
    public void deveRetornarBadRequestQuandoPayloadDeCriacaoForInvalido() throws
            Exception {
        String requestBody = """
                {
                  "name": "",
                  "price": "9.99",
                  "cost": "3.99"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(successFalse())
                .andExpect(errorCode("VALIDATION_ERROR"))
                .andExpect(errorMessageExists());

    }

    @Test
    public void deveRetornarConflictQuandoCriarProdutoDuplicado() throws
            Exception {
        when(productService.create(any(CreateProductRequest.class))).thenThrow(new ProductAlreadyExistsException());

        String requestBody = """
                {
                  "name": "ProdutoTeste",
                  "price": "9.99",
                  "cost": "3.99"
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("PRODUCT_ALREADY_EXISTS"))
                .andExpect(errorMessageExists());
    }

    @Test
    public void deveAtivarProdutoComSucesso() throws
            Exception {
        mockMvc.perform(patch("/api/v1/products/1/activate"))
                .andExpect(status().isOk())
                .andExpect(successTrue());
        verify(productService).activate(1L);
    }

    @Test
    public void deveDesativarProdutoComSucesso() throws
            Exception {
        mockMvc.perform(patch("/api/v1/products/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(successTrue());

        verify(productService).deactivate(1L);
    }

    @Test
    public void deveRetornarConflictQuandoAtivarProdutoJaAtivo() throws
            Exception {
        doThrow(new InvalidProductStateTransitionException()).when(productService).activate(1L);

        mockMvc.perform(patch("/api/v1/products/1/activate"))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_PRODUCT_STATE_TRANSITION"))
                .andExpect(errorMessageExists());
    }

    @Test
    public void deveRetornarConflictQuandoDesativarProdutoJaDesativado() throws
            Exception {
        doThrow(new InvalidProductStateTransitionException()).when(productService).deactivate(1L);

        mockMvc.perform(patch("/api/v1/products/1/deactivate"))
                .andExpect(status().isConflict())
                .andExpect(successFalse())
                .andExpect(errorCode("INVALID_PRODUCT_STATE_TRANSITION"))
                .andExpect(errorMessageExists());
    }

    @Test
    public void deveRetornarNotFoundQuandoAtivarProdutoInexistente() throws
            Exception {
        doThrow(new ProductNotFoundException()).when(productService).activate(1L);

        mockMvc.perform(patch("/api/v1/products/1/activate"))
                .andExpect(status().isNotFound())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    public void deveRetornarNotFoundQuandoDesativarProdutoInexistente() throws
            Exception {
        doThrow(new ProductNotFoundException()).when(productService).deactivate(1L);

        mockMvc.perform(patch("/api/v1/products/1/deactivate"))
                .andExpect(status().isNotFound())
                .andExpect(errorCode("PRODUCT_NOT_FOUND"));
    }

    @Test
    public void deveRetornarConflictQuandoOcorrerLockOtimista() throws
            Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Product.class, 1L)).when(productService).activate(1L);

        mockMvc.perform(patch("/api/v1/products/1/activate"))
                .andExpect(status().isConflict())
                .andExpect(errorCode("OPTIMISTIC_LOCK_FAILURE"));
    }

    @Test
    public void deveRetornarErroInternoQuandoErroInesperadoOcorrer() throws
            Exception {
        when(productService.findAll()).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isInternalServerError())
                .andExpect(errorCode("INTERNAL_SERVER_ERROR"));
    }

}

