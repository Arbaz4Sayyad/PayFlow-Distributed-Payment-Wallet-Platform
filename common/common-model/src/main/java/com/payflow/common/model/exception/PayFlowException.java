package com.payflow.common.model.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Base Runtime Exception for all PayFlow domain and infrastructure failures.
 */
public class PayFlowException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final Map<String, Object> details;

    public PayFlowException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = Collections.emptyMap();
    }

    public PayFlowException(String errorCode, String message, int httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details != null ? details : Collections.emptyMap();
    }

    public PayFlowException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = Collections.emptyMap();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
