package com.payflow.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundPaymentRequest(
        @NotBlank(message = "Refund reason is required")
        String reason
) {
}
