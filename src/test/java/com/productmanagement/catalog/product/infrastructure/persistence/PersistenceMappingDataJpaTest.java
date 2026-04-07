package com.productmanagement.catalog.product.infrastructure.persistence;

import com.productmanagement.catalog.product.domain.model.Product;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryChange;
import com.productmanagement.catalog.product.domain.model.ProductMonetaryField;
import com.productmanagement.catalog.product.domain.model.ProductName;
import com.productmanagement.catalog.product.domain.model.ProductStatus;
import com.productmanagement.catalog.product.domain.model.ProductStockMovement;
import com.productmanagement.catalog.product.domain.model.StockMovementType;
import com.productmanagement.catalog.product.domain.repository.ProductMonetaryChangeRepository;
import com.productmanagement.catalog.product.domain.repository.ProductRepository;
import com.productmanagement.catalog.product.domain.repository.ProductStockMovementRepository;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.catalog.product.support.ProductMonetaryChangeBuilder;
import com.productmanagement.shared.valueobject.Money;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceMappingDataJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMonetaryChangeRepository productMonetaryChangeRepository;

    @Autowired
    private ProductStockMovementRepository productStockMovementRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void devePersistirProdutoComConversorDeNomeEMoney() {
        Product product = ProductBuilder.aProduct()
                .withName("Produto Teste")
                .withPrice("10")
                .withCost("4.50")
                .build();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(ProductName.of("produto teste"), loaded.getName());
        assertEquals(Money.of("10.00"), loaded.getPrice());
        assertEquals(Money.of("4.50"), loaded.getCost());
        assertTrue(productRepository.existsByName(ProductName.of("produto teste")));
    }

    @Test
    void devePersistirProdutoComCamposDeAuditoria() {
        Product product = ProductBuilder.aProduct().build();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(loaded.getCreatedAt());
        assertNotNull(loaded.getVersion());
        assertTrue(loaded.getCreatedAt().isBefore(Instant.now()));
    }

    @Test
    void devePersistirProdutoInativoComStatusCorreto() {
        Product product = ProductBuilder.aProduct().inactive().build();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(ProductStatus.INACTIVE, loaded.getStatus());
    }

    @Test
    void devePersistirProdutoComEstoque() {
        Product product = ProductBuilder.aProduct().withCurrentStock(15).build();

        Product saved = productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(15, loaded.getCurrentStock());
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

    @Test
    void devePersistirAlteracaoDePrecoComMapeamentoCompleto() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Preco").withPrice("20.00").withCost("8.00").build()
        );

        ProductMonetaryChange change = ProductMonetaryChangeBuilder.aPriceChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("20.00"))
                .withNewValue(Money.of("25.00"))
                .build();

        ProductMonetaryChange saved = productMonetaryChangeRepository.saveAndFlush(change);
        entityManager.clear();

        ProductMonetaryChange loaded = productMonetaryChangeRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(loaded.getId());
        assertEquals(ProductMonetaryField.PRICE, loaded.getMonetaryField());
        assertEquals(Money.of("25.00"), loaded.getNewValue());
        assertEquals(Money.of("20.00"), loaded.getOldValue());
        assertNotNull(loaded.getCreatedAt());
        assertEquals(product.getId(), loaded.getProduct().getId());
    }

    @Test
    void devePersistirAlteracaoDeCustoComMapeamentoCompleto() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Custo").withPrice("20.00").withCost("8.00").build()
        );

        ProductMonetaryChange change = ProductMonetaryChangeBuilder.aCostChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("8.00"))
                .withNewValue(Money.of("10.00"))
                .build();

        ProductMonetaryChange saved = productMonetaryChangeRepository.saveAndFlush(change);
        entityManager.clear();

        ProductMonetaryChange loaded = productMonetaryChangeRepository.findById(saved.getId()).orElseThrow();

        assertEquals(ProductMonetaryField.COST, loaded.getMonetaryField());
        assertEquals(Money.of("10.00"), loaded.getNewValue());
        assertEquals(Money.of("8.00"), loaded.getOldValue());
    }

    @Test
    void deveRetornarAlteracoesMonetariasEmOrdemDecrescente() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Ordem").withPrice("10.00").withCost("5.00").build()
        );

        ProductMonetaryChange first = ProductMonetaryChangeBuilder.aPriceChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("10.00"))
                .withNewValue(Money.of("12.00"))
                .withCreatedAt(Instant.now().minusSeconds(60))
                .build();
        ProductMonetaryChange second = ProductMonetaryChangeBuilder.aPriceChange()
                .withId(null)
                .withProduct(product)
                .withOldValue(Money.of("12.00"))
                .withNewValue(Money.of("15.00"))
                .withCreatedAt(Instant.now())
                .build();

        productMonetaryChangeRepository.saveAndFlush(first);
        productMonetaryChangeRepository.saveAndFlush(second);
        entityManager.clear();

        List<ProductMonetaryChange> changes = productMonetaryChangeRepository
                .findByProductIdAndMonetaryField(product.getId(), ProductMonetaryField.PRICE, Sort.by("createdAt").descending());

        assertEquals(2, changes.size());
        assertTrue(changes.get(0).getCreatedAt().isAfter(changes.get(1).getCreatedAt()));
        assertEquals(Money.of("15.00"), changes.get(0).getNewValue());
        assertEquals(Money.of("12.00"), changes.get(1).getNewValue());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemAlteracoesDoTipoBuscado() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Sem Alteracao").withPrice("10.00").withCost("5.00").build()
        );

        productMonetaryChangeRepository.saveAndFlush(
                ProductMonetaryChangeBuilder.aPriceChange()
                        .withId(null)
                        .withProduct(product)
                        .withOldValue(Money.of("10.00"))
                        .withNewValue(Money.of("12.00"))
                        .build()
        );
        entityManager.clear();

        List<ProductMonetaryChange> costChanges = productMonetaryChangeRepository
                .findByProductIdAndMonetaryField(product.getId(), ProductMonetaryField.COST, Sort.by("createdAt").descending());

        assertTrue(costChanges.isEmpty());
    }

    @Test
    void devePersistirMovimentacaoDeEntradaComMapeamentoCompleto() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Estoque Entrada").build()
        );

        ProductStockMovement movement = product.inbound(10);
        productRepository.saveAndFlush(product);
        ProductStockMovement saved = productStockMovementRepository.saveAndFlush(movement);
        entityManager.clear();

        ProductStockMovement loaded = productStockMovementRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(loaded.getId());
        assertEquals(StockMovementType.INBOUND, loaded.getType());
        assertEquals(10, loaded.getQuantity());
        assertNotNull(loaded.getCreatedAt());
        assertEquals(product.getId(), loaded.getProduct().getId());
    }

    @Test
    void devePersistirMovimentacaoDeSaidaComMapeamentoCompleto() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Estoque Saida").withCurrentStock(20).build()
        );

        ProductStockMovement movement = product.outbound(5);
        productRepository.saveAndFlush(product);
        ProductStockMovement saved = productStockMovementRepository.saveAndFlush(movement);
        entityManager.clear();

        ProductStockMovement loaded = productStockMovementRepository.findById(saved.getId()).orElseThrow();

        assertEquals(StockMovementType.OUTBOUND, loaded.getType());
        assertEquals(5, loaded.getQuantity());
    }

    @Test
    void deveRetornarTodasAsMovimentacoesQuandoTipoForNulo() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Todos Tipos").withCurrentStock(10).build()
        );

        ProductStockMovement inbound = product.inbound(5);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(inbound);

        ProductStockMovement outbound = product.outbound(3);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(outbound);

        entityManager.clear();

        List<ProductStockMovement> all = productStockMovementRepository
                .findAllByProductIdAndOptionalType(product.getId(), null);

        assertEquals(2, all.size());
    }

    @Test
    void deveRetornarApenasMovimentacoesDeEntradaQuandoTipoForInbound() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Filtro Entrada").withCurrentStock(10).build()
        );

        ProductStockMovement inbound1 = product.inbound(3);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(inbound1);

        ProductStockMovement inbound2 = product.inbound(4);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(inbound2);

        ProductStockMovement outbound = product.outbound(2);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(outbound);

        entityManager.clear();

        List<ProductStockMovement> inbounds = productStockMovementRepository
                .findAllByProductIdAndOptionalType(product.getId(), StockMovementType.INBOUND);

        assertEquals(2, inbounds.size());
        assertTrue(inbounds.stream().allMatch(m -> m.getType() == StockMovementType.INBOUND));
    }

    @Test
    void deveRetornarApenasMovimentacoesDeSaidaQuandoTipoForOutbound() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Filtro Saida").withCurrentStock(20).build()
        );

        ProductStockMovement inbound = product.inbound(5);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(inbound);

        ProductStockMovement outbound1 = product.outbound(3);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(outbound1);

        ProductStockMovement outbound2 = product.outbound(2);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(outbound2);

        entityManager.clear();

        List<ProductStockMovement> outbounds = productStockMovementRepository
                .findAllByProductIdAndOptionalType(product.getId(), StockMovementType.OUTBOUND);

        assertEquals(2, outbounds.size());
        assertTrue(outbounds.stream().allMatch(m -> m.getType() == StockMovementType.OUTBOUND));
    }

    @Test
    void deveRetornarMovimentacoesEmOrdemDecrescentePorData() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Ordem Estoque").withCurrentStock(10).build()
        );

        ProductStockMovement first = product.inbound(2);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(first);

        ProductStockMovement second = product.inbound(3);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(second);

        entityManager.clear();

        List<ProductStockMovement> movements = productStockMovementRepository
                .findAllByProductIdAndOptionalType(product.getId(), StockMovementType.INBOUND);

        assertEquals(2, movements.size());
        assertTrue(movements.get(0).getCreatedAt().compareTo(movements.get(1).getCreatedAt()) >= 0);
    }

    @Test
    void deveRetornarListaVaziaQuandoProdutoNaoTemMovimentacoes() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Sem Movimentacao").build()
        );
        entityManager.clear();

        List<ProductStockMovement> movements = productStockMovementRepository
                .findAllByProductIdAndOptionalType(product.getId(), null);

        assertTrue(movements.isEmpty());
    }

    @Test
    void deveAtualizarEstoqueNoBancoAposMovimentacao() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Estoque Persistido").build()
        );

        ProductStockMovement inbound = product.inbound(10);
        productRepository.saveAndFlush(product);
        productStockMovementRepository.saveAndFlush(inbound);
        entityManager.clear();

        Product loaded = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(10, loaded.getCurrentStock());

        ProductStockMovement outbound = loaded.outbound(4);
        productRepository.saveAndFlush(loaded);
        productStockMovementRepository.saveAndFlush(outbound);
        entityManager.clear();

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(6, updated.getCurrentStock());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveLancarExcecaoDeOptimisticLockingEmAtualizacoesConcorrentes() {
        Long productId = transactionTemplate.execute(status -> {
            Product product = ProductBuilder.aProduct().withName("Produto Lock").build();
            return productRepository.saveAndFlush(product).getId();
        });

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            Product first = transactionTemplate.execute(status ->
                    productRepository.findById(productId).orElseThrow()
            );
            Product second = transactionTemplate.execute(status ->
                    productRepository.findById(productId).orElseThrow()
            );

            transactionTemplate.execute(status -> {
                first.inbound(5);
                productRepository.saveAndFlush(first);
                return null;
            });

            transactionTemplate.execute(status -> {
                second.inbound(3);
                productRepository.saveAndFlush(second);
                return null;
            });
        });
    }

    @Test
    void deveIncrementarVersaoAcadaAtualizacao() {
        Product product = productRepository.saveAndFlush(
                ProductBuilder.aProduct().withName("Produto Versao").build()
        );
        Long initialVersion = product.getVersion();

        product.inbound(5);
        productRepository.saveAndFlush(product);
        entityManager.clear();

        Product loaded = productRepository.findById(product.getId()).orElseThrow();
        assertTrue(loaded.getVersion() > initialVersion);
    }
}