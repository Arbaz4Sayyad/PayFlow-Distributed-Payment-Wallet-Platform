package com.payflow.ledger.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.ledger.domain.entity.JournalEntryLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record JournalLineResponse(
        UUID id,
        UUID walletId,
        LedgerEntryType entryType,
        BigDecimal amount,
        long amountMinor,
        String formattedAmount,
        Instant createdAt
) {
    public static JournalLineResponse fromEntity(JournalEntryLine line, Currency currency) {
        Money money = Money.of(line.getAmountMinor(), currency);
        return new JournalLineResponse(
                line.getId(),
                line.getWalletId(),
                line.getEntryType(),
                money.toBigDecimal(),
                line.getAmountMinor(),
                money.formatDisplay(),
                line.getCreatedAt()
        );
    }
}
