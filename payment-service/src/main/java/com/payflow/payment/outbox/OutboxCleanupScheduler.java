package com.payflow.payment.outbox;

import com.payflow.payment.domain.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class OutboxCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxCleanupScheduler.class);

    private final OutboxEventRepository outboxEventRepository;

    @Value("${payflow.outbox.retention-days:7}")
    private int retentionDays;

    public OutboxCleanupScheduler(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    /**
     * Nightly cleanup task: Purges outbox events marked PUBLISHED older than the retention period.
     * Prevents unbounded table growth while preserving recent audit history.
     */
    @Scheduled(cron = "${payflow.outbox.cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void purgeOldEvents() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("Running outbox cleanup for PUBLISHED events older than {}", cutoff);

        int deletedCount = outboxEventRepository.purgeProcessedEvents(cutoff);
        log.info("Outbox cleanup complete. Purged {} stale published events.", deletedCount);
    }
}
