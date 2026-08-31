package com.payflow.payment.event;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.PaymentType;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.KafkaTopics;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.common.model.event.payload.PaymentInitiatedPayload;
import com.payflow.payment.domain.entity.Payment;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventPublisher Kafka Messaging Unit Tests")
class PaymentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    private ArgumentCaptor<DomainEvent<?>> eventCaptor;

    private PaymentEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PaymentEventPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish PaymentInitiated event partitioned by senderWalletId")
    void shouldPublishPaymentInitiated() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        Payment payment = new Payment(sender, recipient, 50000L, Currency.INR, PaymentType.P2P_TRANSFER, "KAFKA-KEY-1");

        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.PAYMENT_INITIATED, 0), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq(KafkaTopics.PAYMENT_INITIATED), eq(sender.toString()), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        publisher.publishPaymentInitiated(payment);

        verify(kafkaTemplate).send(eq(KafkaTopics.PAYMENT_INITIATED), eq(sender.toString()), eventCaptor.capture());
        DomainEvent<?> captured = eventCaptor.getValue();

        assertThat(captured.eventType()).isEqualTo("PaymentInitiated");
        assertThat(captured.aggregateType()).isEqualTo("Payment");
        assertThat(captured.correlationId()).isNotBlank();
        assertThat(captured.payload()).isInstanceOf(PaymentInitiatedPayload.class);

        PaymentInitiatedPayload payload = (PaymentInitiatedPayload) captured.payload();
        assertThat(payload.amountMinor()).isEqualTo(50000L);
        assertThat(payload.currency()).isEqualTo(Currency.INR);
    }

    @Test
    @DisplayName("Should publish PaymentCompleted event with gateway reference")
    void shouldPublishPaymentCompleted() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        Payment payment = new Payment(sender, recipient, 10000L, Currency.INR, PaymentType.P2P_TRANSFER, "KAFKA-KEY-2");

        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.PAYMENT_COMPLETED, 1), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq(KafkaTopics.PAYMENT_COMPLETED), eq(sender.toString()), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        publisher.publishPaymentCompleted(payment, "PGW-12345");

        verify(kafkaTemplate).send(eq(KafkaTopics.PAYMENT_COMPLETED), eq(sender.toString()), eventCaptor.capture());
        DomainEvent<?> captured = eventCaptor.getValue();

        assertThat(captured.eventType()).isEqualTo("PaymentCompleted");
        PaymentCompletedPayload payload = (PaymentCompletedPayload) captured.payload();
        assertThat(payload.gatewayTransactionId()).isEqualTo("PGW-12345");
    }

    @Test
    @DisplayName("Should publish PaymentFailed event on transaction decline")
    void shouldPublishPaymentFailed() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        Payment payment = new Payment(sender, recipient, 10000L, Currency.INR, PaymentType.P2P_TRANSFER, "KAFKA-KEY-3");

        when(kafkaTemplate.send(eq(KafkaTopics.PAYMENT_FAILED), eq(sender.toString()), any()))
                .thenReturn(new CompletableFuture<>());

        publisher.publishPaymentFailed(payment, "Insufficient credit limit");

        verify(kafkaTemplate).send(eq(KafkaTopics.PAYMENT_FAILED), eq(sender.toString()), eventCaptor.capture());
        DomainEvent<?> captured = eventCaptor.getValue();

        assertThat(captured.eventType()).isEqualTo("PaymentFailed");
    }
}
