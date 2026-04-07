package com.productmanagement.catalog.product.infrastructure.persistence;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.catalog.product.domain.repository.ProductMonetaryChangeRepository;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.catalog.product.support.ProductMonetaryChangeBuilder;
import com.productmanagement.shared.valueobject.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceMappingDataJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMonetaryChangeRepository productMonetaryChangeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void devePersistirProdutoComConversorDeNomeEMoney() {
        Product product = ProductBuilder.aProduct().withName("Produto Teste").withPrice("10").withCost("4.50").build();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(ProductName.of("produto teste"), loaded.getName());
        assertEquals(Money.of("10.00"), loaded.getPrice());
        assertEquals(Money.of("4.50"), loaded.getCost());
        assertTrue(productRepository.existsByName(ProductName.of("produto teste")));
    }

    @Test
    void deveBuscarAlteracoesMonetariasPorTipo() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto A").withPrice("10.00").withCost("5.00").build()
        );

        ProductMonetaryChange priceChange = ProductMonetaryChangeBuilder.aPriceChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("10.00"))
                .withNewValue(Money.of("12.00"))
                .build();
        ProductMonetaryChange costChange = ProductMonetaryChangeBuilder.aCostChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("5.00"))
                .withNewValue(Money.of("6.00"))
                .build();

        productMonetaryChangeRepository.saveAndFlush(priceChange);
        productMonetaryChangeRepository.saveAndFlush(costChange);
        entityManager.clear();

        List<ProductMonetaryChange> priceChanges = productMonetaryChangeRepository
                .findByProductIdAndMonetaryField(product.getId(), ProductMonetaryField.PRICE, Sort.by("createdAt").descending());
        List<ProductMonetaryChange> costChanges = productMonetaryChangeRepository
                .findByProductIdAndMonetaryField(product.getId(), ProductMonetaryField.COST, Sort.by("createdAt").descending());

        assertEquals(1, priceChanges.size());
        assertEquals(ProductMonetaryField.PRICE, priceChanges.getFirst().getMonetaryField());
        assertEquals(Money.of("12.00"), priceChanges.getFirst().getNewValue());

        assertEquals(1, costChanges.size());
        assertEquals(ProductMonetaryField.COST, costChanges.getFirst().getMonetaryField());
        assertEquals(Money.of("6.00"), costChanges.getFirst().getNewValue());
    }
}
