package com.productmanagement.catalog.product.application.service;

import com.productmanagement.catalog.product.domain.exception.CostUpperPriceException;
import com.productmanagement.catalog.product.domain.exception.PriceBelowCostException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import com.productmanagement.catalog.product.domain.repository.ProductMonetaryChangeRepository;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.catalog.product.support.ProductMonetaryChangeBuilder;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class ProductMonetaryChangeServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMonetaryChangeRepository productMonetaryChangeRepository;

    @InjectMocks
    private ProductMonetaryChangeService productMonetaryChangeService;

    @Test
    public void deveAlterarPrecoDoProduto() {
        Long productId = 1L;
        Money novoPreco = Money.of("18.99");
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMonetaryChangeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProductMonetaryChange result = productMonetaryChangeService.changePrice(productId, novoPreco);
        ArgumentCaptor<ProductMonetaryChange> changeCaptor = ArgumentCaptor.forClass(ProductMonetaryChange.class);

        assertEquals(product, result.getProduct());
        assertEquals(novoPreco, result.getNewValue());
        assertEquals(Money.of("9.99"), result.getOldValue());
        assertEquals(ProductMonetaryField.PRICE, result.getMonetaryField());

        verify(productMonetaryChangeRepository).save(changeCaptor.capture());
        ProductMonetaryChange savedChange = changeCaptor.getValue();
        assertEquals(product, savedChange.getProduct());
        assertEquals(novoPreco, savedChange.getNewValue());
        assertEquals(Money.of("9.99"), savedChange.getOldValue());
        assertEquals(ProductMonetaryField.PRICE, savedChange.getMonetaryField());

    }

    @Test
    public void deveAlterarCustoDoProduto() {
        Long productId = 1L;
        Money novoCusto = Money.of("2.99");
        Money custoAnterior = Money.of("3.99");
        Product product = ProductBuilder.aProduct().build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMonetaryChangeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProductMonetaryChange result = productMonetaryChangeService.changeCost(productId, novoCusto);
        ArgumentCaptor<ProductMonetaryChange> changeCaptor = ArgumentCaptor.forClass(ProductMonetaryChange.class);

        assertEquals(product, result.getProduct());
        assertEquals(novoCusto, result.getNewValue());
        assertEquals(custoAnterior, result.getOldValue());
        assertEquals(ProductMonetaryField.COST, result.getMonetaryField());

        verify(productMonetaryChangeRepository).save(changeCaptor.capture());
        ProductMonetaryChange savedChange = changeCaptor.getValue();
        assertEquals(product, savedChange.getProduct());
        assertEquals(novoCusto, savedChange.getNewValue());
        assertEquals(custoAnterior, savedChange.getOldValue());
        assertEquals(ProductMonetaryField.COST, savedChange.getMonetaryField());

    }

    @Test
    public void deveLancarExcecaoAoTentarAlterarPrecoDeProdutoInexistente() {
        Long productId = 1L;
        Money novoPreco = Money.of("18.99");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productMonetaryChangeService.changePrice(productId, novoPreco));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productMonetaryChangeRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoTentarAlterarCustoDeProdutoInexistente() {
        Long productId = 1L;
        Money novoCusto = Money.of("2.99");

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productMonetaryChangeService.changeCost(productId, novoCusto));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productMonetaryChangeRepository, never()).save(any());
    }

    @Test
    public void deveListarAlteracoesDePreco() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();
        List<ProductMonetaryChange> changeList = List.of(
                ProductMonetaryChangeBuilder.aPriceChange()
                        .withProduct(product)
                        .withOldValue(Money.of("8.99"))
                        .withNewValue(Money.of("9.99"))
                        .build(),
                ProductMonetaryChangeBuilder.aPriceChange()
                        .withProduct(product)
                        .withOldValue(Money.of("9.99"))
                        .withNewValue(Money.of("18.99"))
                        .build()
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMonetaryChangeRepository.findByProductIdAndMonetaryField(productId,
                ProductMonetaryField.PRICE,
                Sort.by("createdAt").descending()))
                .thenReturn(changeList);

        List<ProductMonetaryChange> result = productMonetaryChangeService.priceChanges(productId);

        assertEquals(2, result.size());
        assertEquals(changeList, result);
        verify(productRepository).findById(productId);
        verify(productMonetaryChangeRepository).findByProductIdAndMonetaryField(productId,
                ProductMonetaryField.PRICE,
                Sort.by("createdAt").descending());

    }

    @Test
    public void deveListarAlteracoesDeCusto() {
        Long productId = 1L;
        Product product = ProductBuilder.aProduct().build();
        List<ProductMonetaryChange> changeList = List.of(
                ProductMonetaryChangeBuilder.aCostChange()
                        .withProduct(product)
                        .withOldValue(Money.of("8.99"))
                        .withNewValue(Money.of("9.99"))
                        .build(),
                ProductMonetaryChangeBuilder.aCostChange()
                        .withProduct(product)
                        .withOldValue(Money.of("9.99"))
                        .withNewValue(Money.of("18.99"))
                        .build()
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMonetaryChangeRepository.findByProductIdAndMonetaryField(productId, ProductMonetaryField.COST, Sort.by("createdAt").descending()))
                .thenReturn(changeList);

        List<ProductMonetaryChange> result = productMonetaryChangeService.costChanges(productId);

        assertEquals(2, result.size());
        assertEquals(changeList, result);
        verify(productRepository).findById(productId);
        verify(productMonetaryChangeRepository).findByProductIdAndMonetaryField(productId,
                ProductMonetaryField.COST,
                Sort.by("createdAt").descending());

    }

    @Test
    public void deveLancarExcecaoAoTentarAlterarPrecoAbaixoDoCusto() {
        Long productId = 1L;
        Money novoPreco = Money.of("1.99");
        Product product = ProductBuilder.aProduct().withCost("2.99").build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(PriceBelowCostException.class, () -> productMonetaryChangeService.changePrice(productId, novoPreco));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productMonetaryChangeRepository, never()).save(any());
    }

    @Test
    public void deveLancarExcecaoAoTentarAlterarCustoAcimaDoPreco() {
        Long productId = 1L;
        Money novoCusto = Money.of("10.99");
        Product product = ProductBuilder.aProduct().withPrice("9.99").build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(CostUpperPriceException.class, () -> productMonetaryChangeService.changeCost(productId, novoCusto));

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
        verify(productMonetaryChangeRepository, never()).save(any());

    }

    @Test
    public void deveLancarExcecaoAoTentarListarAlteracoesDePrecoDeProdutoInexistente() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productMonetaryChangeService.priceChanges(productId));
        verify(productRepository).findById(productId);
        verify(productMonetaryChangeRepository, never()).findByProductIdAndMonetaryField(any(), any(), any());
    }

    @Test
    public void deveLancarExcecaoAoTentarListarAlteracoesDeCustoDeProdutoInexistente() {
        Long productId = 1L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productMonetaryChangeService.costChanges(productId));
        verify(productRepository).findById(productId);
        verify(productMonetaryChangeRepository, never()).findByProductIdAndMonetaryField(any(), any(), any());
    }
}
