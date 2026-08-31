package com.payflow.ledger.consumer;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalEntryResponse;
import com.payflow.ledger.inbox.InboxService;
import com.payflow.ledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCompletedConsumer Idempotent Processing Tests")
class PaymentCompletedConsumerTest {

    @Mock
    private LedgerService ledgerService;

    @Mock
    private InboxService inboxService;

    @Mock
    private Acknowledgment acknowledgment;

    @Captor
    private ArgumentCaptor<CreateJournalEntryRequest> requestCaptor;

    private PaymentCompletedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentCompletedConsumer(ledgerService, inboxService);
    }

    @Test
    @DisplayName("Should successfully process fresh payment completed event and record journal entry")
    void shouldProcessFreshEvent() {
        UUID paymentId = UUID.randomUUID();
        UUID senderWalletId = UUID.randomUUID();
        UUID recipientWalletId = UUID.randomUUID();

        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                paymentId,
                senderWalletId,
                recipientWalletId,
                15000L, // 150.00 INR
                Currency.INR,
                "PGW-98765"
        );

        DomainEvent<PaymentCompletedPayload> event = DomainEvent.of(
                "PaymentCompleted",
                "Payment",
                paymentId.toString(),
                payload
        );

        when(inboxService.isAlreadyProcessed(event.eventId(), PaymentCompletedConsumer.CONSUMER_NAME))
                .thenReturn(false);

        JournalEntryResponse response = new JournalEntryResponse(
                UUID.randomUUID(),
                paymentId,
                "test",
                Currency.INR,
                Instant.now(),
                List.of(),
                true
        );
        when(ledgerService.recordJournalEntry(any())).thenReturn(response);

        consumer.onPaymentCompleted(event, acknowledgment);

        // Verify journal entry lines are created and balanced
        verify(ledgerService).recordJournalEntry(requestCaptor.capture());
        CreateJournalEntryRequest captured = requestCaptor.getValue();
        assertThat(captured.transactionId()).isEqualTo(paymentId);
        assertThat(captured.lines()).hasSize(2);
        assertThat(captured.lines().get(0).walletId()).isEqualTo(senderWalletId);
        assertThat(captured.lines().get(0).entryType()).isEqualTo(LedgerEntryType.DEBIT);
        assertThat(captured.lines().get(0).amount()).isEqualByComparingTo(new BigDecimal("150.00"));

        assertThat(captured.lines().get(1).walletId()).isEqualTo(recipientWalletId);
        assertThat(captured.lines().get(1).entryType()).isEqualTo(LedgerEntryType.CREDIT);
        assertThat(captured.lines().get(1).amount()).isEqualByComparingTo(new BigDecimal("150.00"));

        // Verify marked as processed in inbox
        verify(inboxService).markProcessed(eq(event.eventId()), eq(PaymentCompletedConsumer.CONSUMER_NAME));

        // Verify Kafka offset acknowledged
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should skip duplicate event and immediately acknowledge offset without modifying ledger")
    void shouldSkipDuplicateEvent() {
        UUID paymentId = UUID.randomUUID();
        PaymentCompletedPayload payload = new PaymentCompletedPayload(
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                5000L,
                Currency.INR,
                "PGW-DUP"
        );

        DomainEvent<PaymentCompletedPayload> event = DomainEvent.of(
                "PaymentCompleted",
                "Payment",
                paymentId.toString(),
                payload
        );

        // Simulate event already exists in inbox
        when(inboxService.isAlreadyProcessed(event.eventId(), PaymentCompletedConsumer.CONSUMER_NAME))
                .thenReturn(true);

        consumer.onPaymentCompleted(event, acknowledgment);

        // Verify ledger was NEVER touched
        verify(ledgerService, never()).recordJournalEntry(any());
        verify(inboxService, never()).markProcessed(any(), any());

        // Offset is acknowledged to remove poisoned/duplicate message from consumer stream
        verify(acknowledgment).acknowledge();
    }
}
