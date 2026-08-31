package com.payflow.payment.outbox;

import com.payflow.common.model.event.KafkaTopics;
import com.payflow.payment.domain.entity.OutboxEvent;
import com.payflow.payment.domain.repository.OutboxEventRepository;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxPoller Transactional Outbox Unit Tests")
class OutboxPollerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxPoller outboxPoller;

    @BeforeEach
    void setUp() {
        outboxPoller = new OutboxPoller(outboxEventRepository, kafkaTemplate);
        ReflectionTestUtils.setField(outboxPoller, "batchSize", 50);
        ReflectionTestUtils.setField(outboxPoller, "maxRetries", 5);
    }

    @Test
    @DisplayName("Should poll pending events, dispatch to Kafka, and mark PUBLISHED")
    void shouldProcessPendingEventsSuccessfully() {
        UUID eventId = UUID.randomUUID();
        String aggregateId = UUID.randomUUID().toString();
        OutboxEvent event = new OutboxEvent(eventId, "Payment", aggregateId, "PaymentCompleted", "{\"data\":\"test\"}");

        when(outboxEventRepository.findPendingEventsForProcessing(50)).thenReturn(List.of(event));

        RecordMetadata metadata = new RecordMetadata(new TopicPartition(KafkaTopics.PAYMENT_COMPLETED, 0), 0L, 0, 0L, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(null, metadata);
        when(kafkaTemplate.send(eq(KafkaTopics.PAYMENT_COMPLETED), eq(aggregateId), eq("{\"data\":\"test\"}")))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        outboxPoller.processOutbox();

        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
        assertThat(event.getProcessedAt()).isNotNull();
        verify(outboxEventRepository).save(event);
    }

    @Test
    @DisplayName("Should increment retryCount and mark FAILED after exceeding max retries")
    void shouldHandleKafkaPublishFailureAndMarkFailed() {
        UUID eventId = UUID.randomUUID();
        String aggregateId = UUID.randomUUID().toString();
        OutboxEvent event = new OutboxEvent(eventId, "Payment", aggregateId, "PaymentInitiated", "{\"data\":\"test\"}");
        // Simulate already retried 4 times
        for (int i = 0; i < 4; i++) {
            event.recordFailure(5);
        }
        assertThat(event.getRetryCount()).isEqualTo(4);

        when(outboxEventRepository.findPendingEventsForProcessing(anyInt())).thenReturn(List.of(event));

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unreachable"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        outboxPoller.processOutbox();

        // 5th failure exceeds maxRetries (5) -> status becomes FAILED
        assertThat(event.getRetryCount()).isEqualTo(5);
        assertThat(event.getStatus()).isEqualTo("FAILED");
        verify(outboxEventRepository).save(event);
    }
}
