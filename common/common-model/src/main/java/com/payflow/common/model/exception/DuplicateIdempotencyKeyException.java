package com.payflow.common.model.exception;

import java.util.Map;

public class DuplicateIdempotencyKeyException extends PayFlowException {

    public DuplicateIdempotencyKeyException(String idempotencyKey, String message) {
        super(
                "DUPLICATE_IDEMPOTENCY_KEY",
                message != null ? message : String.format("Request with Idempotency-Key '%s' is currently processing or payload mismatch", idempotencyKey),
                409,
                Map.of("idempotencyKey", idempotencyKey)
        );
    }
}
