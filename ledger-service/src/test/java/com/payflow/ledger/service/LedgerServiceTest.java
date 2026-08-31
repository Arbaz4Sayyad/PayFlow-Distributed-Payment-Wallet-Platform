package com.payflow.ledger.service;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.common.model.exception.CurrencyMismatchException;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.ledger.domain.entity.JournalEntry;
import com.payflow.ledger.domain.repository.JournalEntryLineRepository;
import com.payflow.ledger.domain.repository.JournalEntryRepository;
import com.payflow.ledger.dto.AuditBalanceReport;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalEntryResponse;
import com.payflow.ledger.dto.JournalLineRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService Double-Entry Invariant Unit Tests")
class LedgerServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(journalEntryRepository, journalEntryLineRepository);
    }

    @Test
    @DisplayName("Should successfully record balanced 2-leg journal entry: Debit == Credit")
    void shouldRecordBalancedTwoLegEntry() {
        UUID txId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Transfer Wallet A -> Wallet B",
                Currency.INR,
                List.of(
                        new JournalLineRequest(walletA, LedgerEntryType.DEBIT, new BigDecimal("100.00"), Currency.INR),
                        new JournalLineRequest(walletB, LedgerEntryType.CREDIT, new BigDecimal("100.00"), Currency.INR)
                )
        );

        when(journalEntryRepository.findByTransactionIdWithLines(txId)).thenReturn(Optional.empty());
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        JournalEntryResponse response = ledgerService.recordJournalEntry(request);

        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(txId);
        assertThat(response.isBalanced()).isTrue();
        assertThat(response.lines()).hasSize(2);

        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("Should successfully record multi-leg balanced journal entry with platform fee")
    void shouldRecordMultiLegBalancedEntry() {
        UUID txId = UUID.randomUUID();
        UUID senderWallet = UUID.randomUUID();
        UUID recipientWallet = UUID.randomUUID();
        UUID feeWallet = UUID.randomUUID();

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Merchant payment with 2% fee",
                Currency.INR,
                List.of(
                        new JournalLineRequest(senderWallet, LedgerEntryType.DEBIT, new BigDecimal("100.00"), Currency.INR),
                        new JournalLineRequest(recipientWallet, LedgerEntryType.CREDIT, new BigDecimal("98.00"), Currency.INR),
                        new JournalLineRequest(feeWallet, LedgerEntryType.CREDIT, new BigDecimal("2.00"), Currency.INR)
                )
        );

        when(journalEntryRepository.findByTransactionIdWithLines(txId)).thenReturn(Optional.empty());
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        JournalEntryResponse response = ledgerService.recordJournalEntry(request);

        assertThat(response).isNotNull();
        assertThat(response.isBalanced()).isTrue();
        assertThat(response.lines()).hasSize(3);
        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("Should strictly reject unbalanced journal entry: Debits != Credits")
    void shouldRejectUnbalancedEntry() {
        UUID txId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        // 100.00 DEBIT vs 90.00 CREDIT (10.00 unaccounted for!)
        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Unbalanced transaction attempt",
                Currency.INR,
                List.of(
                        new JournalLineRequest(walletA, LedgerEntryType.DEBIT, new BigDecimal("100.00"), Currency.INR),
                        new JournalLineRequest(walletB, LedgerEntryType.CREDIT, new BigDecimal("90.00"), Currency.INR)
                )
        );

        when(journalEntryRepository.findByTransactionIdWithLines(txId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.recordJournalEntry(request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("Double-entry invariant violated")
                .hasFieldOrPropertyWithValue("errorCode", "UNBALANCED_JOURNAL_ENTRY");

        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("Should reject line with currency mismatched from journal currency")
    void shouldRejectCurrencyMismatch() {
        UUID txId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Cross currency line",
                Currency.INR,
                List.of(
                        new JournalLineRequest(walletA, LedgerEntryType.DEBIT, new BigDecimal("100.00"), Currency.INR),
                        new JournalLineRequest(walletB, LedgerEntryType.CREDIT, new BigDecimal("100.00"), Currency.USD)
                )
        );

        when(journalEntryRepository.findByTransactionIdWithLines(txId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledgerService.recordJournalEntry(request))
                .isInstanceOf(CurrencyMismatchException.class);

        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("Should idempotently return existing journal entry for duplicate transactionId")
    void shouldReturnExistingEntryForDuplicateTransactionId() {
        UUID txId = UUID.randomUUID();
        JournalEntry existing = new JournalEntry(txId, "Existing transaction", Currency.INR);

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Duplicate request",
                Currency.INR,
                List.of(
                        new JournalLineRequest(UUID.randomUUID(), LedgerEntryType.DEBIT, new BigDecimal("50.00"), Currency.INR),
                        new JournalLineRequest(UUID.randomUUID(), LedgerEntryType.CREDIT, new BigDecimal("50.00"), Currency.INR)
                )
        );

        when(journalEntryRepository.findByTransactionIdWithLines(txId)).thenReturn(Optional.of(existing));

        JournalEntryResponse response = ledgerService.recordJournalEntry(request);

        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(txId);
        verify(journalEntryRepository, never()).save(any(JournalEntry.class));
    }

    @Test
    @DisplayName("Should verify ledger integrity when all transactions are balanced")
    void shouldVerifyLedgerIntegrityWhenHealthy() {
        when(journalEntryRepository.findUnbalancedTransactionIds()).thenReturn(List.of());

        AuditBalanceReport report = ledgerService.verifyLedgerIntegrity();

        assertThat(report.isIntact()).isTrue();
        assertThat(report.unbalancedTransactionCount()).isEqualTo(0);
        assertThat(report.unbalancedTransactionIds()).isEmpty();
    }
}
