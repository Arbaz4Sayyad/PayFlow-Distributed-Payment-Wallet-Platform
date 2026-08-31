package com.payflow.notification.inbox;

import com.payflow.notification.domain.entity.ProcessedEvent;
import com.payflow.notification.domain.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationInboxService {

    private static final Logger log = LoggerFactory.getLogger(NotificationInboxService.class);

    private final ProcessedEventRepository processedEventRepository;

    public NotificationInboxService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(UUID eventId, String consumerName) {
        return processedEventRepository.existsByEventIdAndConsumerName(eventId, consumerName);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void markProcessed(UUID eventId, String consumerName) {
        ProcessedEvent event = new ProcessedEvent(eventId, consumerName);
        processedEventRepository.save(event);
        log.debug("Marked event {} as processed for consumer {}", eventId, consumerName);
    }
}
