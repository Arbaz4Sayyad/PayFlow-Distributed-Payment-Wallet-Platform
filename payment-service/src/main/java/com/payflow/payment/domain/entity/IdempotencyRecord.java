package com.payflow.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_idempotency_key", columnNames = {"user_id", "idempotency_key"})
        }
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(nullable = false, length = 32)
    private String status; // PENDING, COMPLETED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, UUID userId, String requestHash, Instant expiresAt) {
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.requestHash = requestHash;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public void markCompleted(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.status = "COMPLETED";
    }

    public void markFailed(int statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.status = "FAILED";
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdempotencyRecord that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
