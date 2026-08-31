package com.payflow.ledger.service;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.common.model.exception.CurrencyMismatchException;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.ledger.domain.entity.JournalEntry;
import com.payflow.ledger.domain.entity.JournalEntryLine;
import com.payflow.ledger.domain.repository.JournalEntryLineRepository;
import com.payflow.ledger.domain.repository.JournalEntryRepository;
import com.payflow.ledger.dto.AuditBalanceReport;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalEntryResponse;
import com.payflow.ledger.dto.JournalLineRequest;
import com.payflow.ledger.dto.JournalLineResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public LedgerService(
            JournalEntryRepository journalEntryRepository,
            JournalEntryLineRepository journalEntryLineRepository
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
    }

    /**
     * Records an immutable double-entry journal entry.
     * Enforces the fundamental accounting equation: Sum(Debits) == Sum(Credits).
     */
    @Transactional
    public JournalEntryResponse recordJournalEntry(CreateJournalEntryRequest request) {
        // Idempotency check: if already recorded for this transaction, return existing entry
        var existing = journalEntryRepository.findByTransactionIdWithLines(request.transactionId());
        if (existing.isPresent()) {
            log.info("Idempotent hit: Journal entry already exists for transactionId: {}", request.transactionId());
            return JournalEntryResponse.fromEntity(existing.get());
        }

        Currency currency = request.currency();
        long totalDebitsMinor = 0L;
        long totalCreditsMinor = 0L;

        JournalEntry entry = new JournalEntry(request.transactionId(), request.description(), currency);

        for (JournalLineRequest lineReq : request.lines()) {
            if (lineReq.currency() != currency) {
                throw new CurrencyMismatchException(currency, lineReq.currency());
            }

            Money money = Money.fromBigDecimal(lineReq.amount(), currency);
            long amountMinor = money.amountMinor();

            if (lineReq.entryType() == LedgerEntryType.DEBIT) {
                totalDebitsMinor += amountMinor;
            } else if (lineReq.entryType() == LedgerEntryType.CREDIT) {
                totalCreditsMinor += amountMinor;
            }

            JournalEntryLine line = new JournalEntryLine(lineReq.walletId(), lineReq.entryType(), amountMinor);
            entry.addLine(line);
        }

        // Enforce Double-Entry Invariant: Sum(Debits) MUST EQUAL Sum(Credits)
        if (totalDebitsMinor != totalCreditsMinor) {
            log.error("DOUBLE-ENTRY INVARIANT VIOLATION for tx {}: Debits={} minor, Credits={} minor",
                    request.transactionId(), totalDebitsMinor, totalCreditsMinor);
            throw new PayFlowException(
                    "UNBALANCED_JOURNAL_ENTRY",
                    String.format("Double-entry invariant violated: Total debits (%d minor units) do not equal total credits (%d minor units)",
                            totalDebitsMinor, totalCreditsMinor),
                    400
            );
        }

        entry = journalEntryRepository.save(entry);
        log.info("Recorded journal entry {} for tx {}. Total balanced amount: {} minor units ({} lines)",
                entry.getId(), request.transactionId(), totalDebitsMinor, entry.getLines().size());

        return JournalEntryResponse.fromEntity(entry);
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse getJournalByTransactionId(UUID transactionId) {
        JournalEntry entry = journalEntryRepository.findByTransactionIdWithLines(transactionId)
                .orElseThrow(() -> new PayFlowException(
                        "JOURNAL_ENTRY_NOT_FOUND",
                        "Journal entry for transaction ID " + transactionId + " not found",
                        404
                ));
        return JournalEntryResponse.fromEntity(entry);
    }

    @Transactional(readOnly = true)
    public Page<JournalLineResponse> getWalletStatement(UUID walletId, Pageable pageable) {
        return journalEntryLineRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
                .map(line -> JournalLineResponse.fromEntity(line, line.getJournalEntry().getCurrency()));
    }

    @Transactional(readOnly = true)
    public AuditBalanceReport verifyLedgerIntegrity() {
        List<UUID> unbalanced = journalEntryRepository.findUnbalancedTransactionIds();
        if (unbalanced.isEmpty()) {
            log.info("Ledger integrity audit PASSED: All transactions are strictly balanced.");
            return AuditBalanceReport.healthy();
        } else {
            log.error("CRITICAL: Ledger integrity audit FAILED! Unbalanced transactions: {}", unbalanced);
            return AuditBalanceReport.compromised(unbalanced);
        }
    }
}
