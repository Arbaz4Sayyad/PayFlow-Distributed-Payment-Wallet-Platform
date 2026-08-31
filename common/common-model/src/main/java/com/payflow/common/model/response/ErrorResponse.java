package com.payflow.common.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        Map<String, Object> details,
        List<ValidationError> validationErrors
) {
    public static ErrorResponse of(String code, String message, int status, String path) {
        return new ErrorResponse(code, message, status, path, null, null);
    }

    public static ErrorResponse of(String code, String message, int status, String path, Map<String, Object> details) {
        return new ErrorResponse(code, message, status, path, details, null);
    }

    public static ErrorResponse validation(String path, List<ValidationError> validationErrors) {
        return new ErrorResponse("VALIDATION_ERROR", "Request validation failed", 400, path, null, validationErrors);
    }
}
