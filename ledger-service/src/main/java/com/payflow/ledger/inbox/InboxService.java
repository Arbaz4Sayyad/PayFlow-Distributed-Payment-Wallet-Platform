package com.payflow.ledger.inbox;

import com.payflow.ledger.domain.entity.ProcessedEvent;
import com.payflow.ledger.domain.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InboxService {

    private static final Logger log = LoggerFactory.getLogger(InboxService.class);

    private final ProcessedEventRepository processedEventRepository;

    public InboxService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Checks if an event has already been committed by this consumer.
     */
    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(UUID eventId, String consumerName) {
        return processedEventRepository.existsByEventIdAndConsumerName(eventId, consumerName);
    }

    /**
     * Marks an event as processed within the active business transaction.
     * Guaranteed atomicity: if the business logic rolls back, this record also rolls back!
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void markProcessed(UUID eventId, String consumerName) {
        ProcessedEvent processedEvent = new ProcessedEvent(eventId, consumerName);
        processedEventRepository.save(processedEvent);
        log.debug("Marked event {} as processed for consumer {}", eventId, consumerName);
    }
}
