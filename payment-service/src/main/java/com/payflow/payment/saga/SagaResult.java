package com.payflow.payment.saga;

import com.payflow.common.model.enums.SagaStatus;

import java.util.UUID;

public record SagaResult(
        UUID sagaId,
        String correlationId,
        SagaStatus status,
        String finalStep,
        String failureReason
) {
    public static SagaResult success(UUID sagaId, String correlationId) {
        return new SagaResult(sagaId, correlationId, SagaStatus.COMPLETED, "COMPLETED", null);
    }

    public static SagaResult failed(UUID sagaId, String correlationId, String step, String reason) {
        return new SagaResult(sagaId, correlationId, SagaStatus.FAILED, step, reason);
    }

    public static SagaResult compensated(UUID sagaId, String correlationId, String step, String reason) {
        return new SagaResult(sagaId, correlationId, SagaStatus.COMPENSATED, step, reason);
    }

    public boolean isSuccessful() {
        return status == SagaStatus.COMPLETED;
    }
}
