package com.payflow.common.model.exception;

import java.util.Map;

public class InvalidStateTransitionException extends PayFlowException {

    public InvalidStateTransitionException(String entityType, String fromState, String toState) {
        super(
                "INVALID_STATE_TRANSITION",
                String.format("Cannot transition %s from %s to %s", entityType, fromState, toState),
                400,
                Map.of("entityType", entityType, "fromState", fromState, "toState", toState)
        );
    }

    public InvalidStateTransitionException(Enum<?> fromState, Enum<?> toState) {
        this(
                fromState != null ? fromState.getClass().getSimpleName() : "Entity",
                fromState != null ? fromState.name() : "UNKNOWN",
                toState != null ? toState.name() : "UNKNOWN"
        );
    }
}
