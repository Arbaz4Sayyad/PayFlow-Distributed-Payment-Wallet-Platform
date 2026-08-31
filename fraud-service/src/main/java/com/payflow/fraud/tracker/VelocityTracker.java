package com.payflow.fraud.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VelocityTracker {

    private static final Logger log = LoggerFactory.getLogger(VelocityTracker.class);

    private record TxRecord(Instant timestamp, long amountMinor) {}

    // In-memory sliding window store (Thread-safe)
    private final Map<UUID, List<TxRecord>> userTxHistory = new ConcurrentHashMap<>();

    public void recordTransaction(UUID userId, long amountMinor) {
        userTxHistory.compute(userId, (id, list) -> {
            List<TxRecord> records = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
            records.add(new TxRecord(Instant.now(), amountMinor));
            // Evict records older than 24 hours to prevent memory bloat
            Instant cutoff = Instant.now().minus(Duration.ofHours(24));
            records.removeIf(r -> r.timestamp().isBefore(cutoff));
            return records;
        });
        log.debug("Recorded velocity event for user {}: {} minor units", userId, amountMinor);
    }

    public int getTransactionCount(UUID userId, Duration window) {
        List<TxRecord> records = userTxHistory.get(userId);
        if (records == null || records.isEmpty()) {
            return 0;
        }

        Instant cutoff = Instant.now().minus(window);
        synchronized (records) {
            return (int) records.stream()
                    .filter(r -> r.timestamp().isAfter(cutoff))
                    .count();
        }
    }

    public long getCumulativeVolumeMinor(UUID userId, Duration window) {
        List<TxRecord> records = userTxHistory.get(userId);
        if (records == null || records.isEmpty()) {
            return 0L;
        }

        Instant cutoff = Instant.now().minus(window);
        synchronized (records) {
            return records.stream()
                    .filter(r -> r.timestamp().isAfter(cutoff))
                    .mapToLong(TxRecord::amountMinor)
                    .sum();
        }
    }

    public void clearHistory(UUID userId) {
        userTxHistory.remove(userId);
    }
}
