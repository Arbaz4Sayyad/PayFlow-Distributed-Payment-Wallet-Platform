package com.payflow.merchant.domain.entity;

import com.payflow.merchant.domain.enums.MerchantStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "business_type", nullable = false, length = 64)
    private String businessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MerchantStatus status;

    @Column(name = "settlement_bank_account", length = 64)
    private String settlementBankAccount;

    @Column(name = "settlement_ifsc_routing", length = 32)
    private String settlementIfscRouting;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MerchantApiKey> apiKeys = new ArrayList<>();

    protected Merchant() {
    }

    public Merchant(UUID userId, String businessName, String businessType) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.businessName = businessName;
        this.businessType = businessType;
        this.status = MerchantStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void suspend(String reason) {
        if (this.status != MerchantStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE merchants can be suspended");
        }
        this.status = MerchantStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void reactivate() {
        if (this.status != MerchantStatus.SUSPENDED) {
            throw new IllegalStateException("Only SUSPENDED merchants can be reactivated");
        }
        this.status = MerchantStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void terminate() {
        this.status = MerchantStatus.TERMINATED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getBusinessName() { return businessName; }
    public String getBusinessType() { return businessType; }
    public MerchantStatus getStatus() { return status; }
    public String getSettlementBankAccount() { return settlementBankAccount; }
    public String getSettlementIfscRouting() { return settlementIfscRouting; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<MerchantApiKey> getApiKeys() { return apiKeys; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Merchant that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
