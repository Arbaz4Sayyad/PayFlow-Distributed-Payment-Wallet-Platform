package com.payflow.payment.gateway;

import com.payflow.common.model.currency.Currency;

import java.util.UUID;

public record GatewayRequest(
        UUID paymentId,
        long amountMinor,
        Currency currency,
        String idempotencyKey
) {
}
