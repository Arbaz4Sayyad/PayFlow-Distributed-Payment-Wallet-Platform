package com.payflow.common.model.exception;

public class UnauthorizedException extends PayFlowException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED_ACCESS", message, 403);
    }
}
