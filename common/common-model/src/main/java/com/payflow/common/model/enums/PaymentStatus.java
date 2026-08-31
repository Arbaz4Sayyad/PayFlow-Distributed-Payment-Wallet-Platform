package com.payflow.common.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Payment Transaction Lifecycle States and Finite State Machine transition guards.
 */
public enum PaymentStatus {
    CREATED,
    PROCESSING,
    AUTHORIZED,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUND_PENDING,
    REFUNDED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = Map.of(
            CREATED, EnumSet.of(PROCESSING, FAILED, CANCELLED),
            PROCESSING, EnumSet.of(AUTHORIZED, SUCCESS, FAILED, CANCELLED),
            AUTHORIZED, EnumSet.of(SUCCESS, FAILED, CANCELLED),
            SUCCESS, EnumSet.of(REFUND_PENDING),
            REFUND_PENDING, EnumSet.of(REFUNDED, SUCCESS),
            FAILED, EnumSet.noneOf(PaymentStatus.class),
            CANCELLED, EnumSet.noneOf(PaymentStatus.class),
            REFUNDED, EnumSet.noneOf(PaymentStatus.class)
    );

    public boolean canTransitionTo(PaymentStatus nextState) {
        if (nextState == null) {
            return false;
        }
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(nextState);
    }

    public boolean isTerminal() {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).isEmpty();
    }
}
