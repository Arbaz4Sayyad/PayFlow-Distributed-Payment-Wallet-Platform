package com.payflow.merchant.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateApiKeyRequest(
        @NotBlank(message = "Label is required to identify the API key purpose")
        String label
) {
}
