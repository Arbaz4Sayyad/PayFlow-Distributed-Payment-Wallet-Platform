package com.payflow.ledger.inbox;

import com.payflow.ledger.domain.entity.ProcessedEvent;
import com.payflow.ledger.domain.repository.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InboxService Idempotent Consumer Unit Tests")
class InboxServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private InboxService inboxService;

    @BeforeEach
    void setUp() {
        inboxService = new InboxService(processedEventRepository);
    }

    @Test
    @DisplayName("Should detect previously processed events")
    void shouldDetectDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsByEventIdAndConsumerName(eventId, "TestConsumer")).thenReturn(true);

        assertThat(inboxService.isAlreadyProcessed(eventId, "TestConsumer")).isTrue();
    }

    @Test
    @DisplayName("Should mark fresh event as processed")
    void shouldMarkEventAsProcessed() {
        UUID eventId = UUID.randomUUID();
        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);

        inboxService.markProcessed(eventId, "TestConsumer");

        verify(processedEventRepository).save(captor.capture());
        ProcessedEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(eventId);
        assertThat(saved.getConsumerName()).isEqualTo("TestConsumer");
        assertThat(saved.getProcessedAt()).isNotNull();
    }
}
