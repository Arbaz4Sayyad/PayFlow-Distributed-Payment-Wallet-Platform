package com.payflow.common.model.response;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {
}
