package com.payflow.common.model.enums;

public enum SagaStatus {
    STARTED,
    PROCESSING,
    COMPENSATING,
    COMPENSATED,
    COMPLETED,
    FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == COMPENSATED || this == FAILED;
    }
}
