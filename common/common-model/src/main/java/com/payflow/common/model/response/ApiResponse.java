package com.payflow.common.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error,
        Instant timestamp,
        String traceId
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, Instant.now(), traceId);
    }

    public static <T> ApiResponse<T> error(ErrorResponse error, String traceId) {
        return new ApiResponse<>(false, null, error, Instant.now(), traceId);
    }
}
