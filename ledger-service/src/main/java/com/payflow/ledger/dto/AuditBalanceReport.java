package com.payflow.ledger.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditBalanceReport(
        boolean isIntact,
        int unbalancedTransactionCount,
        List<UUID> unbalancedTransactionIds,
        Instant checkedAt,
        String message
) {
    public static AuditBalanceReport healthy() {
        return new AuditBalanceReport(
                true,
                0,
                List.of(),
                Instant.now(),
                "Double-entry bookkeeping invariant intact: Sum(Debits) == Sum(Credits) across all transactions."
        );
    }

    public static AuditBalanceReport compromised(List<UUID> unbalancedIds) {
        return new AuditBalanceReport(
                false,
                unbalancedIds.size(),
                unbalancedIds,
                Instant.now(),
                "CRITICAL INVARIANT VIOLATION: Found " + unbalancedIds.size() + " unbalanced transactions in the ledger!"
        );
    }
}
