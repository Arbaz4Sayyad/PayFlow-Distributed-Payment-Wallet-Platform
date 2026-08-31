package com.payflow.ledger.consumer;

import com.payflow.common.model.currency.Money;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.common.model.event.DomainEvent;
import com.payflow.common.model.event.KafkaTopics;
import com.payflow.common.model.event.payload.PaymentCompletedPayload;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalLineRequest;
import com.payflow.ledger.inbox.InboxService;
import com.payflow.ledger.service.LedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);
    public static final String CONSUMER_NAME = "ledger-payment-completed-consumer";

    private final LedgerService ledgerService;
    private final InboxService inboxService;

    public PaymentCompletedConsumer(LedgerService ledgerService, InboxService inboxService) {
        this.ledgerService = ledgerService;
        this.inboxService = inboxService;
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "ledger-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onPaymentCompleted(DomainEvent<PaymentCompletedPayload> event, Acknowledgment acknowledgment) {
        log.info("Received PaymentCompleted event {} for payment {}", event.eventId(), event.payload().paymentId());

        // Idempotency check via Inbox Pattern
        if (inboxService.isAlreadyProcessed(event.eventId(), CONSUMER_NAME)) {
            log.info("Duplicate event {} detected by {}. Skipping and acknowledging offset.",
                    event.eventId(), CONSUMER_NAME);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        PaymentCompletedPayload payload = event.payload();
        Money money = Money.of(payload.amountMinor(), payload.currency());

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                payload.paymentId(),
                "Payment settlement " + payload.paymentId() + " (Gateway Tx: " + payload.gatewayTransactionId() + ")",
                payload.currency(),
                List.of(
                        new JournalLineRequest(payload.senderWalletId(), LedgerEntryType.DEBIT, money.toBigDecimal(), payload.currency()),
                        new JournalLineRequest(payload.recipientWalletId(), LedgerEntryType.CREDIT, money.toBigDecimal(), payload.currency())
                )
        );

        // Atomic: Record balanced double-entry lines AND mark inbox processed in the same transaction
        ledgerService.recordJournalEntry(request);
        inboxService.markProcessed(event.eventId(), CONSUMER_NAME);

        log.info("Successfully processed PaymentCompleted event {} into immutable ledger", event.eventId());
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }
}
