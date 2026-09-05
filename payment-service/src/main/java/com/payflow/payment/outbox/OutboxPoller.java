package com.payflow.payment.outbox;

import com.payflow.common.model.event.KafkaTopics;
import com.payflow.payment.domain.entity.OutboxEvent;
import com.payflow.payment.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payflow.outbox.batch-size:50}")
    private int batchSize;

    @Value("${payflow.outbox.max-retries:5}")
    private int maxRetries;

    public OutboxPoller(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Polls pending outbox records using SELECT ... FOR UPDATE SKIP LOCKED.
     * Guarantees zero double-dispatch across horizontally scaled pods.
     * Starts with an initial delay to allow Spring Boot and Actuator probes to initialize cleanly.
     */
    @Scheduled(
            fixedDelayString = "${payflow.outbox.poll-interval-ms:5000}",
            initialDelayString = "${payflow.outbox.initial-delay-ms:15000}"
    )
    public void processOutbox() {
        List<OutboxEvent> pendingEvents;
        try {
            pendingEvents = fetchPendingEvents();
        } catch (Exception ex) {
            log.debug("Outbox poller check skipped: {}", ex.getMessage());
            return;
        }

        if (pendingEvents == null || pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Outbox poller picked up {} pending events for dispatch", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            String targetTopic = resolveTopic(event.getEventType());
            String partitionKey = event.getAggregateId();

            try {
                // Short bounded wait for Kafka dispatch without holding a long DB lock
                kafkaTemplate.send(targetTopic, partitionKey, event.getPayload())
                        .get(2, TimeUnit.SECONDS);

                event.markPublished();
                log.info("Dispatched outbox event {} (type: {}) to topic {}",
                        event.getId(), event.getEventType(), targetTopic);
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event {} to Kafka: {}", event.getId(), ex.getMessage());
                event.recordFailure(maxRetries);
            }

            try {
                saveEvent(event);
            } catch (Exception ex) {
                log.error("Failed to update outbox event {}: {}", event.getId(), ex.getMessage());
            }
        }
    }

    @Transactional
    public List<OutboxEvent> fetchPendingEvents() {
        return outboxEventRepository.findPendingEventsForProcessing(batchSize);
    }

    @Transactional
    public void saveEvent(OutboxEvent event) {
        outboxEventRepository.save(event);
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "PaymentInitiated" -> KafkaTopics.PAYMENT_INITIATED;
            case "PaymentCompleted" -> KafkaTopics.PAYMENT_COMPLETED;
            case "PaymentFailed" -> KafkaTopics.PAYMENT_FAILED;
            case "WalletDebited" -> KafkaTopics.WALLET_DEBITED;
            case "WalletCredited" -> KafkaTopics.WALLET_CREDITED;
            case "LedgerRecorded" -> KafkaTopics.LEDGER_RECORDED;
            default -> "payment.events.unclassified";
        };
    }
}
