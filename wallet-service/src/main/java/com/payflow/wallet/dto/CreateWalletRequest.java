package com.payflow.wallet.dto;

import com.payflow.common.model.currency.Currency;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotNull(message = "Currency is required")
        Currency currency
) {
}
