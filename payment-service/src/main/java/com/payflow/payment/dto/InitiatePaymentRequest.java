package com.payflow.payment.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiatePaymentRequest(
        @NotNull(message = "Sender wallet ID is required")
        UUID senderWalletId,

        @NotNull(message = "Recipient wallet ID is required")
        UUID recipientWalletId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Payment type is required")
        PaymentType paymentType,

        @NotBlank(message = "Idempotency key is required")
        @Size(max = 128, message = "Idempotency key must not exceed 128 characters")
        String idempotencyKey,

        String description
) {
}
