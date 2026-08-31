package com.payflow.payment.domain.entity;

import com.payflow.common.model.enums.SagaStatus;
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
@Table(name = "saga_instances")
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "saga_type", nullable = false, length = 64)
    private String sagaType;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 128)
    private String correlationId;

    @Column(name = "current_state", nullable = false, length = 64)
    private String currentState;

    @Column(name = "current_step", nullable = false, length = 64)
    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SagaStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SagaInstance() {
    }

    public SagaInstance(
            String sagaType,
            String correlationId,
            String initialStep,
            String payload
    ) {
        this.id = UUID.randomUUID();
        this.sagaType = sagaType;
        this.correlationId = correlationId;
        this.currentState = "STARTED";
        this.currentStep = initialStep;
        this.status = SagaStatus.STARTED;
        this.retryCount = 0;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void advanceStep(String nextStep) {
        this.currentStep = nextStep;
        this.currentState = nextStep;
        this.status = SagaStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void startCompensation(String reason) {
        this.currentState = "COMPENSATING: " + reason;
        this.status = SagaStatus.COMPENSATING;
        this.updatedAt = Instant.now();
    }

    public void completeCompensation() {
        this.currentState = "COMPENSATED";
        this.status = SagaStatus.COMPENSATED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.currentState = "COMPLETED";
        this.status = SagaStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        this.currentState = "FAILED: " + reason;
        this.status = SagaStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSagaType() {
        return sagaType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SagaInstance that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
