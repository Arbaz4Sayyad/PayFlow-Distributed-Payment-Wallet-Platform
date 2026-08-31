package com.payflow.fraud.exception;

import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.model.response.ApiResponse;
import com.payflow.common.model.response.ErrorResponse;
import com.payflow.common.model.response.ValidationError;
import com.payflow.common.observability.MdcConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PayFlowException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayFlowException(PayFlowException ex, HttpServletRequest request) {
        String traceId = getTraceId();
        log.warn("PayFlowException: [{}] {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getHttpStatus(),
                request.getRequestURI(),
                ex.getDetails()
        );

        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(errorResponse, traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = getTraceId();
        List<ValidationError> validationErrors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ValidationError(
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage()
            ));
        }

        ErrorResponse errorResponse = ErrorResponse.validation(request.getRequestURI(), validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, traceId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = getTraceId();
        log.error("Unhandled internal exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Trace: " + traceId,
                500,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse, traceId));
    }

    private String getTraceId() {
        String traceId = MDC.get(MdcConstants.TRACE_ID);
        return traceId != null ? traceId : "unknown-trace";
    }
}
