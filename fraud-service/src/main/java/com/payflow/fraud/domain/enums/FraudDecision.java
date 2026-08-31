package com.payflow.fraud.domain.enums;

public enum FraudDecision {
    APPROVED,
    REVIEW,
    REJECTED;

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isReviewRequired() {
        return this == REVIEW;
    }
}
