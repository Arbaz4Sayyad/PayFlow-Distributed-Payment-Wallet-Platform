package com.payflow.common.model.exception;

import com.payflow.common.model.currency.Currency;

import java.util.Map;

public class CurrencyMismatchException extends PayFlowException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super(
                "CURRENCY_MISMATCH",
                String.format("Operation currency mismatch. Expected: %s, Provided: %s", expected, actual),
                400,
                Map.of("expectedCurrency", expected.name(), "actualCurrency", actual.name())
        );
    }
}
