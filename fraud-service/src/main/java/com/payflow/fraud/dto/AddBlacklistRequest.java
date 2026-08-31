package com.payflow.fraud.dto;

import jakarta.validation.constraints.NotBlank;

public record AddBlacklistRequest(
        @NotBlank(message = "Target type is required (USER, MERCHANT, IP, DEVICE_ID, WALLET)")
        String targetType,

        @NotBlank(message = "Target value is required")
        String targetValue,

        @NotBlank(message = "Reason is required")
        String reason
) {
}
