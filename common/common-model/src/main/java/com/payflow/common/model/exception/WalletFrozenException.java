package com.payflow.common.model.exception;

import java.util.Map;
import java.util.UUID;

public class WalletFrozenException extends PayFlowException {

    public WalletFrozenException(UUID walletId, String currentStatus) {
        super(
                "WALLET_FROZEN",
                String.format("Wallet %s is %s and cannot execute transactions", walletId != null ? walletId : "unknown", currentStatus),
                403,
                Map.of("walletId", walletId != null ? walletId.toString() : "unknown", "status", currentStatus != null ? currentStatus : "UNKNOWN")
        );
    }
}
