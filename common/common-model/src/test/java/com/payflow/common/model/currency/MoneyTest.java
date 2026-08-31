package com.payflow.common.model.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Integer Minor-Unit Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should convert human-readable input to exact integer minor units")
    void shouldConvertHumanInputToMinorUnits() {
        Money m = Money.fromBigDecimal(new BigDecimal("1000.50"), Currency.INR);
        assertThat(m.amountMinor()).isEqualTo(100050L);
        assertThat(m.currency()).isEqualTo(Currency.INR);
        assertThat(m.toBigDecimal()).isEqualByComparingTo("1000.50");
    }

    @Test
    @DisplayName("Should reject excess fractional digits beyond currency exponent without silent rounding")
    void shouldRejectExcessFractions() {
        assertThatThrownBy(() -> Money.fromBigDecimal(new BigDecimal("1000.555"), Currency.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeding allowed 2 fraction digits");
    }

    @Test
    @DisplayName("Should correctly perform arithmetic with exact long arithmetic")
    void shouldPerformExactArithmetic() {
        Money m1 = Money.of(150000L, Currency.INR); // ₹1500.00
        Money m2 = Money.of(50025L, Currency.INR);  // ₹500.25

        Money sum = m1.plus(m2);
        assertThat(sum.amountMinor()).isEqualTo(200025L);
        assertThat(sum.toBigDecimal()).isEqualByComparingTo("2000.25");

        Money diff = m1.minus(Money.of(50000L, Currency.INR));
        assertThat(diff.amountMinor()).isEqualTo(100000L);
        assertThat(diff.toBigDecimal()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Should forbid negative monetary values upon creation or subtraction")
    void shouldForbidNegativeMonetaryValues() {
        assertThatThrownBy(() -> Money.of(-100L, Currency.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Monetary value cannot be negative");

        Money m1 = Money.of(1000L, Currency.INR);
        Money m2 = Money.of(2000L, Currency.INR);
        assertThatThrownBy(() -> m1.minus(m2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative balances are forbidden");
    }

    @Test
    @DisplayName("Should reject arithmetic across mismatched currencies")
    void shouldRejectCurrencyMismatch() {
        Money inr = Money.of(10000L, Currency.INR);
        Money usd = Money.of(10000L, Currency.USD);

        assertThatThrownBy(() -> inr.plus(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should format display with currency symbol and decimal formatting")
    void shouldFormatDisplayProperly() {
        Money inr = Money.fromBigDecimal(new BigDecimal("1234.50"), Currency.INR);
        assertThat(inr.formatDisplay()).isEqualTo("₹ 1234.50");

        Money usd = Money.fromBigDecimal(new BigDecimal("99.99"), Currency.USD);
        assertThat(usd.formatDisplay()).isEqualTo("$ 99.99");
    }
}
