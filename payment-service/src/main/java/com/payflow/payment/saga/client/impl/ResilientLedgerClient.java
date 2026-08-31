package com.payflow.payment.saga.client.impl;

import com.payflow.common.model.currency.Currency;
import com.payflow.payment.saga.client.LedgerClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilient implementation of LedgerClient wrapping HTTP calls to ledger-service
 * with Circuit Breaker, Retry, and Bulkhead protection.
 *
 * Ledger recording is idempotent via the inbox pattern on the ledger-service side,
 * so retries are safe — re-recording the same transactionId is a no-op.
 */
@Component
public class ResilientLedgerClient implements LedgerClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientLedgerClient.class);
    private static final String LEDGER_SERVICE = "ledgerService";

    @Override
    @Bulkhead(name = LEDGER_SERVICE, type = Bulkhead.Type.SEMAPHORE,
              fallbackMethod = "recordEntryFallback")
    @Retry(name = LEDGER_SERVICE, fallbackMethod = "recordEntryFallback")
    @CircuitBreaker(name = LEDGER_SERVICE, fallbackMethod = "recordEntryFallback")
    public boolean recordEntry(
            UUID transactionId,
            UUID senderWalletId,
            UUID recipientWalletId,
            long amountMinor,
            Currency currency
    ) {
        log.info("[LedgerClient] RECORD_ENTRY txId={} sender={} recipient={} amount={} {}",
                transactionId, senderWalletId, recipientWalletId, amountMinor, currency);
        // Production: return restTemplate.postForObject(ledgerServiceUrl + "/api/v1/ledger/entries", ...)
        return true;
    }

    // ==========================================
    //  Fallback — invoked when CB is OPEN or retries exhausted
    // ==========================================

    /**
     * Ledger fallback: the Outbox Pattern is our safety net.
     * If the ledger call fails here, the Outbox Poller will re-publish
     * the PaymentCompleted event which the ledger-service consumer will
     * process idempotently once connectivity is restored.
     */
    public boolean recordEntryFallback(
            UUID transactionId,
            UUID senderWalletId,
            UUID recipientWalletId,
            long amountMinor,
            Currency currency,
            Throwable ex
    ) {
        log.error("[LedgerClient FALLBACK] RECORD_ENTRY failed for txId={}: {}. " +
                "Outbox will ensure eventual ledger recording.", transactionId, ex.getMessage());
        return false;
    }
}
