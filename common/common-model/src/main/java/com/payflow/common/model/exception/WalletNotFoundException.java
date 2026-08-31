package com.payflow.common.model.exception;

import java.util.Map;
import java.util.UUID;

public class WalletNotFoundException extends PayFlowException {

    public WalletNotFoundException(UUID walletId) {
        super(
                "WALLET_NOT_FOUND",
                String.format("Wallet with ID %s does not exist", walletId),
                404,
                Map.of("walletId", walletId.toString())
        );
    }
}
