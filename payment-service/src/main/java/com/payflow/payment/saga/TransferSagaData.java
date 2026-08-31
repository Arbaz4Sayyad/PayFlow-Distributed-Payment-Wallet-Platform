package com.payflow.payment.saga;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public record TransferSagaData(
        UUID paymentId,
        UUID senderWalletId,
        UUID recipientWalletId,
        long amountMinor,
        Currency currency,
        String idempotencyKey
) {
}
