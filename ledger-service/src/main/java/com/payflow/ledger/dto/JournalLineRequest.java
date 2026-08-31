package com.payflow.ledger.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.LedgerEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record JournalLineRequest(
        @NotNull(message = "Wallet ID is required")
        UUID walletId,

        @NotNull(message = "Entry type (DEBIT/CREDIT) is required")
        LedgerEntryType entryType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency
) {
}
