package com.payflow.payment.domain.entity;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.PaymentStatus;
import com.payflow.common.model.enums.PaymentType;
import com.payflow.common.model.exception.InvalidStateTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payment_idempotency", columnNames = {"sender_wallet_id", "idempotency_key"})
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_wallet_id", nullable = false)
    private UUID senderWalletId;

    @Column(name = "recipient_wallet_id", nullable = false)
    private UUID recipientWalletId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 32)
    private PaymentType paymentType;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Payment() {
    }

    public Payment(
            UUID senderWalletId,
            UUID recipientWalletId,
            long amountMinor,
            Currency currency,
            PaymentType paymentType,
            String idempotencyKey
    ) {
        this.id = UUID.randomUUID();
        this.senderWalletId = senderWalletId;
        this.recipientWalletId = recipientWalletId;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.status = PaymentStatus.CREATED;
        this.paymentType = paymentType;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Payment(
            UUID id,
            UUID senderWalletId,
            UUID recipientWalletId,
            long amountMinor,
            Currency currency,
            PaymentType paymentType,
            String idempotencyKey
    ) {
        this.id = id;
        this.senderWalletId = senderWalletId;
        this.recipientWalletId = recipientWalletId;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.status = PaymentStatus.CREATED;
        this.paymentType = paymentType;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Strict Finite State Machine transition guard.
     * Throws InvalidStateTransitionException if transition violates the lifecycle.
     */
    public void transitionTo(PaymentStatus newStatus, String failureReason) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        if (failureReason != null) {
            this.failureReason = failureReason;
        }
        this.updatedAt = Instant.now();
    }

    public void transitionTo(PaymentStatus newStatus) {
        transitionTo(newStatus, null);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public UUID getRecipientWalletId() {
        return recipientWalletId;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
