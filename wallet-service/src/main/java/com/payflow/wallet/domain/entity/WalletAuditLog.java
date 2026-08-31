package com.payflow.wallet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "wallet_audit_log")
public class WalletAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, length = 32)
    private String operation; // TOP_UP, WITHDRAW, DEBIT, CREDIT, FREEZE, UNFREEZE

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "balance_before_minor", nullable = false)
    private long balanceBeforeMinor;

    @Column(name = "balance_after_minor", nullable = false)
    private long balanceAfterMinor;

    @Column(name = "reference_id", length = 128)
    private String referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WalletAuditLog() {
    }

    public WalletAuditLog(
            Wallet wallet,
            String operation,
            long amountMinor,
            long balanceBeforeMinor,
            long balanceAfterMinor,
            String referenceId
    ) {
        this.wallet = wallet;
        this.operation = operation;
        this.amountMinor = amountMinor;
        this.balanceBeforeMinor = balanceBeforeMinor;
        this.balanceAfterMinor = balanceAfterMinor;
        this.referenceId = referenceId;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getOperation() {
        return operation;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public long getBalanceBeforeMinor() {
        return balanceBeforeMinor;
    }

    public long getBalanceAfterMinor() {
        return balanceAfterMinor;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletAuditLog that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
