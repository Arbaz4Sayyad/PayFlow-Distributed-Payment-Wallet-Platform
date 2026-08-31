package com.payflow.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterMerchantRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Business name is required")
        String businessName,

        @NotBlank(message = "Business type is required")
        String businessType,

        String settlementBankAccount,
        String settlementIfscRouting
) {
}
