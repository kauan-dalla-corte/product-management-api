package com.productmanagement.catalog.shared.valueobject;

import com.productmanagement.shared.exception.InvalidMonetaryValueException;
import com.productmanagement.shared.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void deveCriarDinheiroAPartirDeStringValida() {
        Money money = Money.of("10.50");

        assertEquals(new BigDecimal("10.50"), money.toBigDecimal());
    }

    @Test
    void deveRemoverEspacosDaStringAoCriarDinheiro() {
        Money money = Money.of("  10.50  ");

        assertEquals(new BigDecimal("10.50"), money.toBigDecimal());
    }

    @Test
    void deveNormalizarEscalaAoCriarAPartirDeString() {
        Money money = Money.of("10");
        assertEquals(new BigDecimal("10.00"), money.toBigDecimal());
    }

    @Test
    void deveNormalizarEscalaAoCriarAPartirDeBigDecimal() {
        Money money = Money.of(new BigDecimal("10"));

        assertEquals(new BigDecimal("10.00"), money.toBigDecimal());
    }

    @Test
    void deveArredondarHalfUpAoCriarDinheiro() {
        Money money = Money.of("10.555");

        assertEquals(new BigDecimal("10.56"), money.toBigDecimal());
    }

    @Test
    void deveRetornarInstanciaZero() {
        Money money = Money.zero();

        assertEquals(new BigDecimal("0.00"), money.toBigDecimal());
        assertTrue(money.isZero());
    }

    @Test
    void deveLancarExcecaoQuandoStringForNula() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of((String) null)
        );

        assertEquals("Value cannot be null or blank", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStringForBlank() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of("   ")
        );

        assertEquals("Value cannot be null or blank", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoStringForInvalida() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of("abc")
        );

        assertEquals("Invalid monetary value: abc", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoBigDecimalForNulo() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of((BigDecimal) null)
        );

        assertEquals("Value cannot be null", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoValorForNegativo() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of("-1.00")
        );

        assertEquals("Value cannot be negative", ex.getMessage());
    }

    @Test
    void deveRetornarFalseQuandoDinheiroNaoForZero() {
        Money money = Money.of("1.00");

        assertFalse(money.isZero());
    }

    @Test
    void deveRetornarTrueQuandoValorForMaiorQueOutro() {
        Money money = Money.of("20.00");
        Money other = Money.of("10.00");

        assertTrue(money.isGreaterThan(other));
    }

    @Test
    void deveRetornarFalseQuandoValorNaoForMaiorQueOutro() {
        Money money = Money.of("10.00");
        Money other = Money.of("20.00");

        assertFalse(money.isGreaterThan(other));
    }

    @Test
    void deveLancarExcecaoAoCompararMaiorQueComOperandoNulo() {
        Money money = Money.of("10.00");

        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> money.isGreaterThan(null)
        );

        assertEquals("Money operand cannot be null", ex.getMessage());
    }

    @Test
    void deveRetornarTrueQuandoValorForMenorQueOutro() {
        Money money = Money.of("10.00");
        Money other = Money.of("20.00");

        assertTrue(money.isLessThan(other));
    }

    @Test
    void deveRetornarFalseQuandoValorNaoForMenorQueOutro() {
        Money money = Money.of("20.00");
        Money other = Money.of("10.00");

        assertFalse(money.isLessThan(other));
    }

    @Test
    void deveLancarExcecaoAoCompararMenorQueComOperandoNulo() {
        Money money = Money.of("10.00");

        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> money.isLessThan(null)
        );

        assertEquals("Money operand cannot be null", ex.getMessage());
    }

    @Test
    void deveSomarValoresMonetarios() {
        Money result = Money.of("10.25").add(Money.of("5.30"));

        assertEquals(new BigDecimal("15.55"), result.toBigDecimal());
    }

    @Test
    void deveLancarExcecaoAoSomarOperandoNulo() {
        Money money = Money.of("10.00");

        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> money.add(null)
        );

        assertEquals("Money operand cannot be null", ex.getMessage());
    }

    @Test
    void deveSubtrairValoresMonetarios() {
        Money result = Money.of("10.50").subtract(Money.of("3.25"));

        assertEquals(new BigDecimal("7.25"), result.toBigDecimal());
    }

    @Test
    void deveLancarExcecaoAoSubtrairOperandoNulo() {
        Money money = Money.of("10.00");

        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> money.subtract(null)
        );

        assertEquals("Money operand cannot be null", ex.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoResultadoDaSubtracaoForNegativo() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of("5.00").subtract(Money.of("10.00"))
        );

        assertEquals("Result cannot be negative", ex.getMessage());
    }

    @Test
    void deveMultiplicarValorMonetarioPorFatorPositivo() {
        Money result = Money.of("10.50").multiply(3);

        assertEquals(new BigDecimal("31.50"), result.toBigDecimal());
    }

    @Test
    void deveMultiplicarValorMonetarioPorZero() {
        Money result = Money.of("10.50").multiply(0);

        assertEquals(new BigDecimal("0.00"), result.toBigDecimal());
        assertTrue(result.isZero());
    }

    @Test
    void deveLancarExcecaoAoMultiplicarPorFatorNegativo() {
        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> Money.of("10.00").multiply(-1)
        );

        assertEquals("Factor cannot be negative", ex.getMessage());
    }

    @Test
    void deveCompararValoresMonetariosCorretamente() {
        Money smaller = Money.of("10.00");
        Money bigger = Money.of("20.00");
        Money equal = Money.of("10.000");

        assertTrue(smaller.compareTo(bigger) < 0);
        assertTrue(bigger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(equal));
    }

    @Test
    void deveLancarExcecaoAoCompararComNull() {
        Money money = Money.of("10.00");

        InvalidMonetaryValueException ex = assertThrows(
                InvalidMonetaryValueException.class,
                () -> money.compareTo(null)
        );

        assertEquals("Money operand cannot be null", ex.getMessage());
    }

    @Test
    void deveSerIgualQuandoValoresDiferiremApenasNaEscala() {
        Money first = Money.of("10.0");
        Money second = Money.of("10.00");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void naoDeveSerIgualQuandoValoresForemDiferentes() {
        Money first = Money.of("10.00");
        Money second = Money.of("11.00");

        assertNotEquals(first, second);
    }

    @Test
    void naoDeveSerIgualANull() {
        Money money = Money.of("10.00");

        assertNotEquals(null, money);
    }

    @Test
    void naoDeveSerIgualAValorMonetarioDiferente() {
        Money money = Money.of("10.00");
        Money other = Money.of("20.00");

        assertNotEquals(money, other);
    }

    @Test
    void deveRetornarRepresentacaoTextoSemNotacaoCientifica() {
        Money money = Money.of("10");

        assertEquals("10.00", money.toString());
    }
}