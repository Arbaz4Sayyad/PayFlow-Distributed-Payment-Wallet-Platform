package com.payflow.common.model.currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Monetary Value Object using integer minor units internally.
 * 
 * Guarantees zero floating-point drift by representing all currency amounts
 * as 64-bit signed integer minor units (e.g. 100050 paise for ₹1000.50 INR).
 * 
 * Range of Long.MAX_VALUE (9,223,372,036,854,775,807 minor units) supports
 * transactions up to ~₹92.2 quadrillion without risk of integer overflow.
 */
public record Money(
        @JsonProperty("amountMinor") long amountMinor,
        @JsonProperty("currency") Currency currency
) implements Comparable<Money>, Serializable {

    @JsonCreator
    public Money {
        Objects.requireNonNull(currency, "Currency must not be null");
        if (amountMinor < 0) {
            throw new IllegalArgumentException("Monetary value cannot be negative: " + amountMinor);
        }
    }

    public static Money of(long amountMinor, Currency currency) {
        return new Money(amountMinor, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    /**
     * Converts a human-readable BigDecimal (e.g. "1000.50") to Money with minor units.
     * Validates that the input scale does not exceed the currency's ISO-4217 fraction exponent.
     * Rejects excess fractional digits without silent rounding.
     */
    public static Money fromBigDecimal(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monetary amount cannot be negative: " + amount);
        }

        // Validate that fractional digits do not exceed currency exponent
        int maxFractionDigits = currency.getDefaultFractionDigits();
        if (amount.scale() > maxFractionDigits) {
            // Check if trailing digits are non-zero
            BigDecimal stripped = amount.stripTrailingZeros();
            if (stripped.scale() > maxFractionDigits) {
                throw new IllegalArgumentException(
                        String.format("Amount %s has scale %d, exceeding allowed %d fraction digits for currency %s",
                                amount.toPlainString(), stripped.scale(), maxFractionDigits, currency)
                );
            }
        }

        BigDecimal scaled = amount.setScale(maxFractionDigits, RoundingMode.UNNECESSARY);
        long minorUnits = scaled.movePointRight(maxFractionDigits).longValueExact();
        return new Money(minorUnits, currency);
    }

    /**
     * Converts from String input at API boundary (e.g. "1500.00").
     */
    public static Money fromString(String amountStr, Currency currency) {
        Objects.requireNonNull(amountStr, "Amount string must not be null");
        return fromBigDecimal(new BigDecimal(amountStr.trim()), currency);
    }

    /**
     * Converts minor units to human-readable BigDecimal with exact currency scale for API responses.
     */
    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(amountMinor, currency.getDefaultFractionDigits());
    }

    public Money plus(Money other) {
        validateSameCurrency(other);
        long result = Math.addExact(this.amountMinor, other.amountMinor);
        return new Money(result, this.currency);
    }

    public Money minus(Money other) {
        validateSameCurrency(other);
        if (this.amountMinor < other.amountMinor) {
            throw new IllegalArgumentException(
                    String.format("Cannot subtract %d from %d: negative balances are forbidden",
                            other.amountMinor, this.amountMinor)
            );
        }
        long result = Math.subtractExact(this.amountMinor, other.amountMinor);
        return new Money(result, this.currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amountMinor > other.amountMinor;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amountMinor >= other.amountMinor;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amountMinor < other.amountMinor;
    }

    public boolean isLessThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amountMinor <= other.amountMinor;
    }

    public boolean isPositive() {
        return this.amountMinor > 0;
    }

    public boolean isZero() {
        return this.amountMinor == 0;
    }

    public String formatDisplay() {
        return currency.getSymbol() + " " + toBigDecimal().toPlainString();
    }

    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "Comparison money must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    String.format("Currency mismatch: cannot perform operation between %s and %s",
                            this.currency, other.currency)
            );
        }
    }

    @Override
    public int compareTo(Money o) {
        validateSameCurrency(o);
        return Long.compare(this.amountMinor, o.amountMinor);
    }

    @Override
    public String toString() {
        return toBigDecimal().toPlainString() + " " + currency.name() + " (" + amountMinor + " minor)";
    }
}
