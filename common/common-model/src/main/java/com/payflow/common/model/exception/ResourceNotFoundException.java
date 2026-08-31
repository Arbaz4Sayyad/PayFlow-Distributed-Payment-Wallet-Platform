package com.payflow.common.model.exception;

public class ResourceNotFoundException extends PayFlowException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, 404);
    }
}
