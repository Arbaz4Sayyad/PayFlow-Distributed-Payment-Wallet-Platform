package com.payflow.ledger.domain.repository;

import com.payflow.ledger.domain.entity.ProcessedEvent;
import com.payflow.ledger.domain.entity.ProcessedEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {

    boolean existsByEventIdAndConsumerName(UUID eventId, String consumerName);
}
