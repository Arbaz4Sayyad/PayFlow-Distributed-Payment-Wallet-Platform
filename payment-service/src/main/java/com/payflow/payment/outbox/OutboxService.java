package com.payflow.payment.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.payment.domain.entity.OutboxEvent;
import com.payflow.payment.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Enqueues an outbox event into the database within the current ACID transaction.
     * Guaranteed atomicity: either both business state and outbox record commit, or both roll back.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(String aggregateType, String aggregateId, String eventType, DomainEvent<?> event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(aggregateType, aggregateId, eventType, payloadJson);
            outboxEventRepository.save(outboxEvent);
            log.debug("Enqueued outbox event {} for aggregate {} [{}]", eventType, aggregateId, aggregateType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for aggregate {}: {}", aggregateId, e.getMessage(), e);
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
    }
}
