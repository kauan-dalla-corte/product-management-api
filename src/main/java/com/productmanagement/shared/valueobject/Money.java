package com.productmanagement.shared.valueobject;

import com.productmanagement.shared.exception.InvalidMonetaryValueException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money implements Comparable<Money> {

    private static final int SCALE = 2;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Column(name = "value", precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    private Money(BigDecimal value) {
        BigDecimal normalizedValue = normalize(value);
        validate(normalizedValue);
        this.value = normalizedValue;
    }

    public static Money of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidMonetaryValueException("Value cannot be null or blank");
        }

        try {
            return new Money(new BigDecimal(value.trim()));
        } catch (NumberFormatException ex) {
            throw new InvalidMonetaryValueException("Invalid monetary value: " + value);
        }
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            throw new InvalidMonetaryValueException("Value cannot be null");
        }
        return value.setScale(SCALE, ROUNDING_MODE);
    }

    private static void validate(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMonetaryValueException("Value cannot be negative");
        }
    }

    private static void validateOperand(Money other) {
        if (other == null) {
            throw new InvalidMonetaryValueException("Money operand cannot be null");
        }
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Money other) {
        validateOperand(other);
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        validateOperand(other);
        return compareTo(other) < 0;
    }

    public Money add(Money other) {
        validateOperand(other);
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        validateOperand(other);

        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMonetaryValueException("Result cannot be negative");
        }

        return new Money(result);
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new InvalidMonetaryValueException("Factor cannot be negative");
        }
        return new Money(this.value.multiply(BigDecimal.valueOf(factor)));
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public int compareTo(Money other) {
        validateOperand(other);
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money other)) {
            return false;
        }
        return value.compareTo(other.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}