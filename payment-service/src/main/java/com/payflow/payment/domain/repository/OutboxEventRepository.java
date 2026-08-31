package com.payflow.payment.domain.repository;

import com.payflow.payment.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Primary Outbox Polling Query: Selects pending events utilizing PostgreSQL's
     * FOR UPDATE SKIP LOCKED to prevent lock contention across multiple replicas.
     */
    @Query(value = """
            SELECT *
            FROM outbox_events
            WHERE status = 'PENDING'
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findPendingEventsForProcessing(@Param("batchSize") int batchSize);

    long countByStatus(String status);

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.status = 'PUBLISHED' AND o.processedAt < :cutoff")
    int purgeProcessedEvents(@Param("cutoff") Instant cutoff);
}
