package com.payflow.payment.service;

import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.PaymentStatus;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.common.model.event.payload.PaymentFailedPayload;
import com.payflow.common.model.event.payload.PaymentInitiatedPayload;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.observability.PayFlowMetrics;
import com.payflow.payment.domain.entity.Payment;
import com.payflow.payment.domain.repository.PaymentRepository;
import com.payflow.payment.dto.InitiatePaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.gateway.GatewayRequest;
import com.payflow.payment.gateway.GatewayResponse;
import com.payflow.payment.gateway.PaymentGatewayProvider;
import com.payflow.payment.outbox.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayProvider gatewayProvider;
    private final OutboxService outboxService;
    private final PayFlowMetrics metrics;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGatewayProvider gatewayProvider,
            OutboxService outboxService,
            PayFlowMetrics metrics
    ) {
        this.paymentRepository = paymentRepository;
        this.gatewayProvider = gatewayProvider;
        this.outboxService = outboxService;
        this.metrics = metrics;
    }

    /**
     * Initiates a payment lifecycle governed by the PaymentStatus Finite State Machine.
     * Guarantees atomic dual-write safety via Transactional Outbox.
     */
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {
        long startMs = System.currentTimeMillis();

        // Idempotency check: if already initiated with this sender & key, return existing payment
        var existing = paymentRepository.findBySenderWalletIdAndIdempotencyKey(
                request.senderWalletId(), request.idempotencyKey()
        );
        if (existing.isPresent()) {
            log.info("Idempotent replay detected for senderWallet: {}, key: {}",
                    request.senderWalletId(), request.idempotencyKey());
            metrics.incrementIdempotentReplay();
            return PaymentResponse.fromEntity(existing.get());
        }

        // Validate self-transfer
        if (request.senderWalletId().equals(request.recipientWalletId())) {
            throw new PayFlowException("INVALID_TRANSFER", "Sender and recipient wallets cannot be identical", 400);
        }

        Money money = Money.fromBigDecimal(request.amount(), request.currency());

        // State 1: CREATED
        Payment payment = new Payment(
                request.senderWalletId(),
                request.recipientWalletId(),
                money.amountMinor(),
                request.currency(),
                request.paymentType(),
                request.idempotencyKey()
        );
        payment = paymentRepository.save(payment);
        log.info("Payment created: ID {}, Amount: {} minor", payment.getId(), payment.getAmountMinor());
        metrics.incrementPaymentInitiated(request.currency().name());

        // Transactional Outbox: Enqueue PaymentInitiated within the same ACID transaction
        DomainEvent<PaymentInitiatedPayload> initiatedEvent = DomainEvent.of(
                "PaymentInitiated",
                "Payment",
                payment.getId().toString(),
                new PaymentInitiatedPayload(
                        payment.getId(),
                        payment.getSenderWalletId(),
                        payment.getRecipientWalletId(),
                        payment.getAmountMinor(),
                        payment.getCurrency(),
                        payment.getIdempotencyKey()
                )
        );
        outboxService.enqueue("Payment", payment.getId().toString(), "PaymentInitiated", initiatedEvent);

        // State 2: PROCESSING
        payment.transitionTo(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Execute external payment gateway or clearing rail
        GatewayRequest gwRequest = new GatewayRequest(
                payment.getId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getIdempotencyKey()
        );
        GatewayResponse gwResponse = gatewayProvider.execute(gwRequest);

        // State 3: SUCCESS or FAILED
        if (gwResponse.success()) {
            payment.transitionTo(PaymentStatus.SUCCESS);
            log.info("Payment {} completed successfully. GatewayTxId: {}",
                    payment.getId(), gwResponse.gatewayTransactionId());
            metrics.incrementPaymentCompleted(payment.getCurrency().name());
            metrics.recordPaymentProcessingTime(
                System.currentTimeMillis() - startMs, "COMPLETED", payment.getCurrency().name());

            DomainEvent<PaymentCompletedPayload> completedEvent = DomainEvent.of(
                    "PaymentCompleted",
                    "Payment",
                    payment.getId().toString(),
                    new PaymentCompletedPayload(
                            payment.getId(),
                            payment.getSenderWalletId(),
                            payment.getRecipientWalletId(),
                            payment.getAmountMinor(),
                            payment.getCurrency(),
                            gwResponse.gatewayTransactionId()
                    )
            );
            outboxService.enqueue("Payment", payment.getId().toString(), "PaymentCompleted", completedEvent);
        } else {
            payment.transitionTo(PaymentStatus.FAILED, gwResponse.errorMessage());
            log.warn("Payment {} failed at gateway: [{}] {}",
                    payment.getId(), gwResponse.errorCode(), gwResponse.errorMessage());
            metrics.incrementPaymentFailed(payment.getCurrency().name());
            metrics.recordPaymentProcessingTime(
                System.currentTimeMillis() - startMs, "FAILED", payment.getCurrency().name());

            DomainEvent<PaymentFailedPayload> failedEvent = DomainEvent.of(
                    "PaymentFailed",
                    "Payment",
                    payment.getId().toString(),
                    new PaymentFailedPayload(
                            payment.getId(),
                            payment.getSenderWalletId(),
                            payment.getRecipientWalletId(),
                            payment.getAmountMinor(),
                            payment.getCurrency(),
                            gwResponse.errorMessage()
                    )
            );
            outboxService.enqueue("Payment", payment.getId().toString(), "PaymentFailed", failedEvent);
        }

        payment = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PayFlowException("PAYMENT_NOT_FOUND", "Payment ID " + paymentId + " not found", 404));
        return PaymentResponse.fromEntity(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PayFlowException("PAYMENT_NOT_FOUND", "Payment ID " + paymentId + " not found", 404));

        // State transition: SUCCESS -> REFUND_PENDING -> REFUNDED (guarded by FSM)
        payment.transitionTo(PaymentStatus.REFUND_PENDING, reason);
        payment.transitionTo(PaymentStatus.REFUNDED, reason);
        payment = paymentRepository.save(payment);

        log.info("Payment {} refunded. Reason: {}", paymentId, reason);
        metrics.incrementPaymentRefunded(payment.getCurrency().name());
        return PaymentResponse.fromEntity(payment);
    }
}
