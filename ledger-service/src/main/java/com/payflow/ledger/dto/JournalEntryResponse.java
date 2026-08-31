package com.payflow.ledger.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.ledger.domain.entity.JournalEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        UUID transactionId,
        String description,
        Currency currency,
        Instant createdAt,
        List<JournalLineResponse> lines,
        boolean isBalanced
) {
    public static JournalEntryResponse fromEntity(JournalEntry entry) {
        List<JournalLineResponse> lineResponses = entry.getLines().stream()
                .map(line -> JournalLineResponse.fromEntity(line, entry.getCurrency()))
                .toList();

        return new JournalEntryResponse(
                entry.getId(),
                entry.getTransactionId(),
                entry.getDescription(),
                entry.getCurrency(),
                entry.getCreatedAt(),
                lineResponses,
                true
        );
    }
}
