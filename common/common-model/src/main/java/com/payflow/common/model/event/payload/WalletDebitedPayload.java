package com.payflow.common.model.event.payload;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public record WalletDebitedPayload(
        UUID walletId,
        long amountMinor,
        Currency currency,
        String referenceId,
        long balanceAfterMinor
) {
}
