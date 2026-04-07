package com.productmanagement.catalog.product.domain.model;

import com.productmanagement.catalog.product.domain.exception.CostUpperPriceException;
import com.productmanagement.catalog.product.domain.exception.InsufficientStockException;
import com.productmanagement.catalog.product.domain.exception.InvalidProductStateTransitionException;
import com.productmanagement.catalog.product.domain.exception.InvalidQuantityException;
import com.productmanagement.catalog.product.domain.exception.PriceBelowCostException;
import com.productmanagement.catalog.product.domain.exception.ProductHasStockException;
import com.productmanagement.catalog.product.domain.exception.SameCostException;
import com.productmanagement.catalog.product.domain.exception.SamePriceException;
import com.productmanagement.catalog.product.support.ProductBuilder;
import com.productmanagement.shared.exception.InvalidMonetaryValueException;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductTest {

    @Test
    public void deveCriarProduto() {
        Product product = ProductBuilder.aProduct().build();
        assertEquals(ProductName.of("ProdutoTeste"), product.getName());
        assertEquals(Money.of("9.99"), product.getPrice());
        assertEquals(Money.of("3.99"), product.getCost());
    }

    @Test
    public void deveInicializarEstoqueComZero() {
        Product product = ProductBuilder.aProduct().build();
        assertEquals(0, product.getCurrentStock());
    }

    @Test
    public void deveInicializarProdutoComStatusAtivo() {
        Product product = ProductBuilder.aProduct().build();
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    @Test
    public void naoDeveCriarProdutoComPrecoNegativo() {
        assertThrows(InvalidMonetaryValueException.class,
                () -> ProductBuilder.aProduct().withPrice("-10.99").build());
    }

    @Test
    public void naoDeveCriarProdutoComCustoNegativo() {
        assertThrows(InvalidMonetaryValueException.class,
                () -> ProductBuilder.aProduct().withCost("-1.99").build());
    }

    @Test
    public void naoDeveCriarProdutoComPrecoZero() {
        assertThrows(InvalidMonetaryValueException.class,
                () -> ProductBuilder.aProduct().withPrice("0").build());
    }

    @Test
    public void naoDeveCriarProdutoComPrecoNulo() {
        assertThrows(InvalidMonetaryValueException.class,
                () -> ProductBuilder.aProduct().withPrice(null).build());
    }

    @Test
    public void naoDevePermitirAlteracoesQuandoProdutoEstiverInativo() {
        Product product = ProductBuilder.aProduct().inactive().build();
        assertThrows(InvalidProductStateTransitionException.class, () -> product.changePrice(Money.of("19.99")));
    }

    @Test
    public void deveAlterarPrecoDoProduto() {
        Product product = ProductBuilder.aProduct().build();
        product.changePrice(Money.of("19.99"));
        assertEquals(Money.of("19.99"), product.getPrice());
    }

    @Test
    public void deveAlterarCustoDoProduto() {
        Product product = ProductBuilder.aProduct().build();
        product.changeCost(Money.of("2.49"));
        assertEquals(Money.of("2.49"), product.getCost());
    }

    @Test
    public void naoDeveAlterarPrecoQuandoForIgualAoAtual() {
        Product product = ProductBuilder.aProduct().withPrice("9.99").build();
        assertThrows(SamePriceException.class, () -> product.changePrice(Money.of("9.99")));
    }

    @Test
    public void naoDeveAlterarCustoQuandoForIgualAoAtual() {
        Product product = ProductBuilder.aProduct().withCost("2.99").build();
        assertThrows(SameCostException.class, () -> product.changeCost(Money.of("2.99")));
    }

    @Test
    public void naoDevePermitirPrecoAbaixoDoCusto() {
        Product product = ProductBuilder.aProduct().withCost("2.99").build();
        assertThrows(PriceBelowCostException.class, () -> product.changePrice(Money.of("1.99")));
    }

    @Test
    public void naoDevePermitirCustoAcimaDoPreco() {
        Product product = ProductBuilder.aProduct().withPrice("9.99").build();
        assertThrows(CostUpperPriceException.class, () -> product.changeCost(Money.of("10.99")));
    }

    @Test
    public void naoDevePermitirAtivarProdutoJaAtivo() {
        Product product = ProductBuilder.aProduct().build();
        assertThrows(InvalidProductStateTransitionException.class, product::activate);
    }

    @Test
    public void naoDevePermitirDesativarProdutoJaInativo() {
        Product product = ProductBuilder.aProduct().inactive().build();
        assertThrows(InvalidProductStateTransitionException.class, product::deactivate);
    }

    @Test
    void naoDevePermitirDesativarProdutoComEstoque() {
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();
        assertThrows(ProductHasStockException.class, product::deactivate);
    }

    @Test
    public void deveAdicionarQuantidadeAoEstoque() {
        Product product = ProductBuilder.aProduct().build();
        product.inbound(10);
        assertEquals(10, product.getCurrentStock());
    }

    @Test
    public void deveRemoverQuantidadeDoEstoque() {
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();
        product.outbound(2);
        assertEquals(8, product.getCurrentStock());
    }

    @Test
    public void naoDevePermitirEstoqueNegativo() {
        Product product = ProductBuilder.aProduct().withCurrentStock(10).build();
        assertThrows(InsufficientStockException.class, () -> product.outbound(11));
    }

    @Test
    public void naoDevePermitirQuantidadeNegativa() {
        Product product = ProductBuilder.aProduct().build();
        assertThrows(InvalidQuantityException.class, () -> product.inbound(-10));
        assertThrows(InvalidQuantityException.class, () -> product.outbound(-10));
    }

}
