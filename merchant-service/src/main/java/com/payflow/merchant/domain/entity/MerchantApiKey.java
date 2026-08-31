package com.payflow.merchant.domain.entity;

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
@Table(name = "merchant_api_keys")
public class MerchantApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    /**
     * SHA-256 hash of the raw API key. The raw key is NEVER persisted.
     * Only shown once to the caller at creation time.
     */
    @Column(name = "api_key_hash", nullable = false, unique = true, length = 255)
    private String apiKeyHash;

    @Column(nullable = false, length = 64)
    private String label;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MerchantApiKey() {
    }

    public MerchantApiKey(Merchant merchant, String apiKeyHash, String label) {
        this.id = UUID.randomUUID();
        this.merchant = merchant;
        this.apiKeyHash = apiKeyHash;
        this.label = label;
        this.revoked = false;
        this.createdAt = Instant.now();
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() { return id; }
    public Merchant getMerchant() { return merchant; }
    public String getApiKeyHash() { return apiKeyHash; }
    public String getLabel() { return label; }
    public boolean isRevoked() { return revoked; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MerchantApiKey that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
