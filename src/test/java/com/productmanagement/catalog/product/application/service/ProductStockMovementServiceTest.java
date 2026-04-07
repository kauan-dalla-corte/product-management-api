package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.domain.exception.InsufficientStockException;
import com.productmanagement.catalog.product.domain.exception.InvalidQuantityException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.domain.repository.ProductStockMovementRepository;
import com.productmanagement.catalog.product.support.ProductBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductStockMovementServiceTest {

    @Mock
    private ProductStockMovementRepository productStockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductStockMovementService productStockMovementService;

    @Test
    public void deveRegistrarEntradaDeEstoque() {
        Long productId = 1L;
        int quantityToAdd = 5;
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStockMovementRepository.save(any(ProductStockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProductStockMovement productStockMovement = productStockMovementService.stockInbound(productId, quantityToAdd);

        assertNotNull(productStockMovement);
        assertEquals(product, productStockMovement.getProduct());
        assertEquals(quantityToAdd, productStockMovement.getQuantity());
        assertEquals(StockMovementType.INBOUND, productStockMovement.getType());
        assertEquals(quantityToAdd, product.getCurrentStock());

        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(productStockMovementRepository).save(productStockMovement);

    }

    @Test
    public void deveRegistrarSaidaDeEstoque() {
        Long productId = 1L;
        int quantityToRemove = 3;
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStockMovementRepository.save(any(ProductStockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProductStockMovement productStockMovement = productStockMovementService.stockOutbound(productId, quantityToRemove);

        assertNotNull(productStockMovement);
        assertEquals(product, productStockMovement.getProduct());
        assertEquals(quantityToRemove, productStockMovement.getQuantity());
        assertEquals(StockMovementType.OUTBOUND, productStockMovement.getType());
        assertEquals(7, product.getCurrentStock());

        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(productStockMovementRepository).save(productStockMovement);
    }

    @Test
    public void deveLancarExcecaoAoRegistrarEntradaParaProdutoInexistente() {
        Long productId = 1L;
        int quantityToAdd = 5;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productStockMovementService.stockInbound(productId, quantityToAdd));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productStockMovementRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoRegistrarSaidaParaProdutoInexistente() {
        Long productId = 1L;
        int quantityToRemove = 5;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productStockMovementService.stockOutbound(productId, quantityToRemove));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productStockMovementRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoRegistrarSaidaComEstoqueInsuficiente() {
        Long productId = 1L;
        int quantityToRemove = 5;
        Product product = ProductBuilder.aProduct().withCurrentStock(3).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> productStockMovementService.stockOutbound(productId, quantityToRemove));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(product);
        verify(productStockMovementRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoRegistrarEntradaComQuantidadeZero() {
        Long productId = 1L;
        int quantityToAdd = 0;
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(InvalidQuantityException.class, () -> productStockMovementService.stockInbound(productId, quantityToAdd));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(product);
        verify(productStockMovementRepository, never()).save(any());
    }

    @Test
    public void deveRetornarMovimentacoesDeEstoqueDoProdutoPorTipo() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();
        ProductStockMovement movement1 = ProductStockMovement.inbound(product, 14);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productStockMovementRepository.findAllByProductIdAndOptionalType(eq(productId), eq(StockMovementType.INBOUND))).thenReturn(List.of(
                movement1));

        List<ProductStockMovement> result = productStockMovementService.stockMovements(productId, StockMovementType.INBOUND);

        assertEquals(1, result.size());
        assertEquals(movement1, result.getFirst());

        verify(productRepository).findById(productId);
        verify(productStockMovementRepository).findAllByProductIdAndOptionalType(eq(productId), eq(StockMovementType.INBOUND));
    }

    @Test
    public void deveLancarExcecaoAoConsultarTodasAsMovimentacoesDeProdutoInexistente() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productStockMovementService.stockMovements(productId, null));

        verify(productRepository).findById(productId);
        verify(productStockMovementRepository, never()).findAllByProductIdAndOptionalType(any(), any());
    }

    @Test
    public void deveLancarExcecaoAoConsultarMovimentacoesPorTipoDeProdutoInexistente() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productStockMovementService.stockMovements(productId, StockMovementType.INBOUND));

        verify(productRepository).findById(productId);
        verify(productStockMovementRepository, never()).findAllByProductIdAndOptionalType(any(), any());
    }

}

