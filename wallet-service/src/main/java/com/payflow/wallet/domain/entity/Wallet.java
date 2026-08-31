package com.payflow.wallet.domain.entity;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.WalletStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "balance_minor", nullable = false)
    private long balanceMinor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private WalletStatus status;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Wallet() {
    }

    public Wallet(UUID userId, Currency currency) {
        this.userId = userId;
        this.currency = currency;
        this.balanceMinor = 0L;
        this.status = WalletStatus.ACTIVE;
        this.version = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Wallet(UUID id, UUID userId, Currency currency) {
        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.balanceMinor = 0L;
        this.status = WalletStatus.ACTIVE;
        this.version = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getBalanceMinor() {
        return balanceMinor;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setBalanceMinor(long balanceMinor) {
        this.balanceMinor = balanceMinor;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return this.status == WalletStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet wallet)) return false;
        return Objects.equals(id, wallet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
