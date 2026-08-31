package com.payflow.fraud.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "blacklist_records")
public class BlacklistRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType; // USER, MERCHANT, IP, DEVICE_ID, WALLET

    @Column(name = "target_value", nullable = false, length = 255)
    private String targetValue;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BlacklistRecord() {
    }

    public BlacklistRecord(String targetType, String targetValue, String reason) {
        this.id = UUID.randomUUID();
        this.targetType = targetType.toUpperCase();
        this.targetValue = targetValue;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlacklistRecord that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
