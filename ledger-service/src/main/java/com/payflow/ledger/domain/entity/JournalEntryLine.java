package com.payflow.ledger.domain.entity;

import com.payflow.common.model.enums.LedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "journal_entry_lines")
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 16)
    private LedgerEntryType entryType;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public JournalEntryLine() {
    }

    public JournalEntryLine(UUID walletId, LedgerEntryType entryType, long amountMinor) {
        this.walletId = walletId;
        this.entryType = entryType;
        this.amountMinor = amountMinor;
        this.createdAt = Instant.now();
    }

    public JournalEntryLine(UUID id, UUID walletId, LedgerEntryType entryType, long amountMinor) {
        this.id = id;
        this.walletId = walletId;
        this.entryType = entryType;
        this.amountMinor = amountMinor;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalEntryLine that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
