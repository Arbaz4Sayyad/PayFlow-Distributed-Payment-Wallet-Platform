package com.payflow.fraud.dto;

import com.payflow.common.model.currency.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FraudEvaluationRequest(
        @NotNull(message = "Transaction ID is required")
        UUID transactionId,

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency,

        String ipAddress,
        String deviceId
) {
}
