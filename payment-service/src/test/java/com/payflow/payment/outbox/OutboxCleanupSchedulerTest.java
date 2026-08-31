package com.payflow.payment.outbox;

import com.payflow.payment.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxCleanupScheduler Stale Event Purge Tests")
class OutboxCleanupSchedulerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxCleanupScheduler cleanupScheduler;

    @Test
    @DisplayName("Should purge published events older than retention period")
    void shouldPurgeStalePublishedEvents() {
        ReflectionTestUtils.setField(cleanupScheduler, "retentionDays", 7);
        when(outboxEventRepository.purgeProcessedEvents(any(Instant.class))).thenReturn(42);

        cleanupScheduler.purgeOldEvents();

        verify(outboxEventRepository).purgeProcessedEvents(any(Instant.class));
    }
}
