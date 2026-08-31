package com.payflow.common.model.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Universal Event-Driven CloudEvents-compliant domain event envelope.
 */
public record DomainEvent<T>(
        UUID eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        Instant occurredAt,
        int version,
        String correlationId,
        T payload
) {
    public static <T> DomainEvent<T> of(
            String eventType,
            String aggregateType,
            String aggregateId,
            String correlationId,
            T payload
    ) {
        return new DomainEvent<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                Instant.now(),
                1,
                correlationId != null ? correlationId : UUID.randomUUID().toString(),
                payload
        );
    }

    public static <T> DomainEvent<T> of(
            String eventType,
            String aggregateType,
            String aggregateId,
            T payload
    ) {
        return of(eventType, aggregateType, aggregateId, null, payload);
    }
}
