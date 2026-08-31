package com.payflow.payment.event;

import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.KafkaTopics;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.common.model.event.payload.PaymentFailedPayload;
import com.payflow.common.model.event.payload.PaymentInitiatedPayload;
import com.payflow.common.observability.MdcConstants;
import com.payflow.payment.domain.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentInitiated(Payment payment) {
        PaymentInitiatedPayload payload = new PaymentInitiatedPayload(
                payment.getId(),
                payment.getSenderWalletId(),
                payment.getRecipientWalletId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                payment.getIdempotencyKey()
        );

        DomainEvent<PaymentInitiatedPayload> event = DomainEvent.of(
                "PaymentInitiated",
                "Payment",
                payment.getId() != null ? payment.getId().toString() : UUID.randomUUID().toString(),
                getTraceId(),
                payload
        );

        // Partition Key: senderWalletId ensures strictly ordered events per customer wallet
        String partitionKey = payment.getSenderWalletId().toString();
        kafkaTemplate.send(KafkaTopics.PAYMENT_INITIATED, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published PaymentInitiatedEvent to topic {} partition {} offset {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish PaymentInitiatedEvent: {}", ex.getMessage(), ex);
                    }
                });
    }

    public void publishPaymentCompleted(Payment payment, String gatewayTxId) {
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                payment.getId(),
                payment.getSenderWalletId(),
                payment.getRecipientWalletId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                gatewayTxId
        );

        DomainEvent<PaymentCompletedPayload> event = DomainEvent.of(
                "PaymentCompleted",
                "Payment",
                payment.getId() != null ? payment.getId().toString() : UUID.randomUUID().toString(),
                getTraceId(),
                payload
        );

        String partitionKey = payment.getSenderWalletId().toString();
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published PaymentCompletedEvent for payment {}", payment.getId());
                    } else {
                        log.error("Failed to publish PaymentCompletedEvent: {}", ex.getMessage(), ex);
                    }
                });
    }

    public void publishPaymentFailed(Payment payment, String reason) {
        PaymentFailedPayload payload = new PaymentFailedPayload(
                payment.getId(),
                payment.getSenderWalletId(),
                payment.getRecipientWalletId(),
                payment.getAmountMinor(),
                payment.getCurrency(),
                reason
        );

        DomainEvent<PaymentFailedPayload> event = DomainEvent.of(
                "PaymentFailed",
                "Payment",
                payment.getId() != null ? payment.getId().toString() : UUID.randomUUID().toString(),
                getTraceId(),
                payload
        );

        String partitionKey = payment.getSenderWalletId().toString();
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, partitionKey, event);
    }

    private String getTraceId() {
        String traceId = MDC.get(MdcConstants.TRACE_ID);
        return traceId != null ? traceId : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
