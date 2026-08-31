package com.payflow.ledger.dto;

import com.payflow.common.model.currency.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateJournalEntryRequest(
        @NotNull(message = "Transaction ID is required")
        UUID transactionId,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotEmpty(message = "At least 2 journal entry lines are required for double-entry bookkeeping")
        @Size(min = 2, message = "Double-entry bookkeeping requires at least 2 balanced lines")
        @Valid
        List<JournalLineRequest> lines
) {
}
