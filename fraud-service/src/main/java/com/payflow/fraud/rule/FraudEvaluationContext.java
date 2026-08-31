package com.payflow.fraud.rule;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public record FraudEvaluationContext(
        UUID transactionId,
        UUID userId,
        UUID walletId,
        long amountMinor,
        Currency currency,
        String ipAddress,
        String deviceId
) {
}
