package com.payflow.ledger.domain.repository;

import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.ledger.domain.entity.JournalEntryLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, UUID> {

    Page<JournalEntryLine> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);

    Page<JournalEntryLine> findByWalletIdAndEntryTypeOrderByCreatedAtDesc(
            UUID walletId,
            LedgerEntryType entryType,
            Pageable pageable
    );

    Page<JournalEntryLine> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID walletId,
            Instant from,
            Instant to,
            Pageable pageable
    );
}
