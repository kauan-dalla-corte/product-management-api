package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.application.dto.request.CreateProductRequest;
import com.productmanagement.catalog.product.domain.exception.InvalidProductStateTransitionException;
import com.productmanagement.catalog.product.domain.exception.ProductAlreadyExistsException;
import com.productmanagement.catalog.product.domain.exception.ProductHasStockException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.catalog.product.domain.model.ProductStatus;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void deveCriarProduto() {
        CreateProductRequest request = new CreateProductRequest("ProdutoTeste", "10", "4.50");

        when(productRepository.existsByName(ProductName.of(request.name()))).thenReturn(false);
        when(productRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Product product = productService.create(request);
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        assertEquals(ProductName.of(request.name()), product.getName());
        assertEquals(Money.of("10.00"), product.getPrice());
        assertEquals(Money.of("4.50"), product.getCost());

        verify(productRepository).saveAndFlush(captor.capture());
        Product savedProduct = captor.getValue();
        assertEquals(ProductName.of(request.name()), savedProduct.getName());
        assertEquals(Money.of("10.00"), savedProduct.getPrice());
        assertEquals(Money.of("4.50"), savedProduct.getCost());

        verify(productRepository).existsByName(ProductName.of(request.name()));
    }

    @Test
    public void naoDevePermitirCriacaoDeProdutoComNomeDuplicado() {
        CreateProductRequest request = new CreateProductRequest("ProdutoTeste", "10", "4.50");

        when(productRepository.existsByName(ProductName.of(request.name()))).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.create(request));
        verify(productRepository).existsByName(ProductName.of(request.name()));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    public void deveTraduzirConflitoDeUnicidadeDoBancoParaRegraDeNegocio() {
        CreateProductRequest request = new CreateProductRequest("ProdutoTeste", "10", "4.50");

        when(productRepository.existsByName(ProductName.of(request.name()))).thenReturn(false);
        when(productRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThrows(ProductAlreadyExistsException.class, () -> productService.create(request));

        verify(productRepository).existsByName(ProductName.of(request.name()));
        verify(productRepository).saveAndFlush(any());
    }

    @Test
    public void deveListarTodosOsProdutosOrdenadosPorId() {

        List<Product> productList = List.of(
                ProductBuilder.aProduct().build(),
                ProductBuilder.aProduct().build()
        );

        when(productRepository.findAll(any(Sort.class))).thenReturn(productList);

        List<Product> result = productService.findAll();

        assertEquals(2, result.size());

        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);
        verify(productRepository).findAll(captor.capture());

        Sort sortUsado = captor.getValue();

        assertEquals("id", sortUsado.iterator().next().getProperty());
        assertEquals(Sort.Direction.ASC, sortUsado.iterator().next().getDirection());
    }

    @Test
    public void deveObterProdutoPorId() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(productId);

        assertEquals(product, result);
        verify(productRepository).findById(productId);
    }

    @Test
    public void deveLancarExcecaoQuandoProdutoNaoForEncontrado() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(productId));

        verify(productRepository).findById(productId);
    }

    @Test
    public void deveDesativarProdutoAtivo() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        productService.deactivate(productId);

        assertEquals(ProductStatus.INACTIVE, product.getStatus());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    public void deveAtivarProdutoInativo() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().inactive().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        productService.activate(productId);

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    public void deveLancarExcecaoAoDesativarProdutoInexistente() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deactivate(productId));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoAtivarProdutoInexistente() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.activate(productId));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoDesativarProdutoJaInativo() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().inactive().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(InvalidProductStateTransitionException.class, () -> productService.deactivate(productId));
        verify(productRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoAtivarProdutoJaAtivo() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(InvalidProductStateTransitionException.class, () -> productService.activate(productId));
        verify(productRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoTentarDesativarProdutoComEstoque() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(ProductHasStockException.class, () -> productService.deactivate(productId));
        verify(productRepository, never()).save(any());

    }

}
