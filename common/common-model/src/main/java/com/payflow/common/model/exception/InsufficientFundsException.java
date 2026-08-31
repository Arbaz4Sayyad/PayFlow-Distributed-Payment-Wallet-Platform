package com.payflow.common.model.exception;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class InsufficientFundsException extends PayFlowException {

    public InsufficientFundsException(UUID walletId, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(
                "INSUFFICIENT_FUNDS",
                String.format("Wallet %s has insufficient funds. Requested: %s, Available: %s",
                        walletId != null ? walletId : "unknown",
                        requestedAmount != null ? requestedAmount.toPlainString() : "0",
                        availableBalance != null ? availableBalance.toPlainString() : "0"),
                409,
                Map.of(
                        "walletId", walletId != null ? walletId.toString() : "unknown",
                        "requestedAmount", requestedAmount != null ? requestedAmount.toPlainString() : "0",
                        "availableBalance", availableBalance != null ? availableBalance.toPlainString() : "0"
                )
        );
    }
}
