package com.payflow.ledger.domain.repository;

import com.payflow.ledger.domain.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByTransactionId(UUID transactionId);

    @Query("SELECT j FROM JournalEntry j LEFT JOIN FETCH j.lines WHERE j.transactionId = :transactionId")
    Optional<JournalEntry> findByTransactionIdWithLines(@Param("transactionId") UUID transactionId);

    boolean existsByTransactionId(UUID transactionId);

    /**
     * Audit Query: Finds any transaction where Total Debits != Total Credits.
     * In a healthy double-entry ledger, this MUST return an empty list.
     */
    @Query("""
            SELECT j.transactionId
            FROM JournalEntry j
            JOIN j.lines l
            GROUP BY j.transactionId
            HAVING SUM(CASE WHEN l.entryType = com.payflow.common.model.enums.LedgerEntryType.CREDIT THEN l.amountMinor ELSE -l.amountMinor END) != 0
            """)
    List<UUID> findUnbalancedTransactionIds();
}
