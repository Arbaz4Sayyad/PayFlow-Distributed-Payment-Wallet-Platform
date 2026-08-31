package com.payflow.payment.dto;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.PaymentStatus;
import com.payflow.common.model.enums.PaymentType;
import com.payflow.payment.domain.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID senderWalletId,
        UUID recipientWalletId,
        BigDecimal amount,
        long amountMinor,
        Currency currency,
        PaymentStatus status,
        PaymentType paymentType,
        String idempotencyKey,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse fromEntity(Payment payment) {
        Money money = Money.of(payment.getAmountMinor(), payment.getCurrency());
        return new PaymentResponse(
                payment.getId(),
                payment.getSenderWalletId(),
                payment.getRecipientWalletId(),
                money.toBigDecimal(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentType(),
                payment.getIdempotencyKey(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
