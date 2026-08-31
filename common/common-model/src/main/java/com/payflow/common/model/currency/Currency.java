package com.payflow.common.model.currency;

/**
 * Supported ISO-4217 Currency Codes in PayFlow.
 */
public enum Currency {
    INR("Indian Rupee", "₹", 2),
    USD("United States Dollar", "$", 2),
    EUR("Euro", "€", 2),
    GBP("British Pound Sterling", "£", 2);

    private final String displayName;
    private final String symbol;
    private final int defaultFractionDigits;

    Currency(String displayName, String symbol, int defaultFractionDigits) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.defaultFractionDigits = defaultFractionDigits;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDefaultFractionDigits() {
        return defaultFractionDigits;
    }
}
