package com.payflow.common.model.event.payload;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public record PaymentFailedPayload(
        UUID paymentId,
        UUID senderWalletId,
        UUID recipientWalletId,
        long amountMinor,
        Currency currency,
        String failureReason
) {
}
