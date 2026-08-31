package com.payflow.fraud.domain.entity;

import com.payflow.fraud.domain.enums.FraudDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "flagged_transactions")
public class FlaggedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FraudDecision decision;

    @Column(name = "triggered_rules", nullable = false, columnDefinition = "TEXT")
    private String triggeredRules;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FlaggedTransaction() {
    }

    public FlaggedTransaction(
            UUID transactionId,
            UUID userId,
            int riskScore,
            FraudDecision decision,
            String triggeredRules
    ) {
        this.id = UUID.randomUUID();
        this.transactionId = transactionId;
        this.userId = userId;
        this.riskScore = riskScore;
        this.decision = decision;
        this.triggeredRules = triggeredRules;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public FraudDecision getDecision() {
        return decision;
    }

    public String getTriggeredRules() {
        return triggeredRules;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlaggedTransaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
