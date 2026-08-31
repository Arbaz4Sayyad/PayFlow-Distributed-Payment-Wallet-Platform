package com.payflow.payment.saga.client.impl;

import com.payflow.common.model.currency.Currency;
import com.payflow.payment.saga.client.WalletClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resilient implementation of WalletClient wrapping HTTP calls to wallet-service
 * with Circuit Breaker, Retry, and Bulkhead protection.
 *
 * Protection hierarchy (outer → inner):
 *   Bulkhead (concurrency limit) → Retry (transient failures) → CircuitBreaker (open on sustained failure)
 *
 * In production, replace the log stubs with RestTemplate / WebClient calls to wallet-service.
 */
@Component
public class ResilientWalletClient implements WalletClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientWalletClient.class);
    private static final String WALLET_SERVICE = "walletService";

    @Override
    @Bulkhead(name = WALLET_SERVICE, type = Bulkhead.Type.SEMAPHORE,
              fallbackMethod = "debitFallback")
    @Retry(name = WALLET_SERVICE, fallbackMethod = "debitFallback")
    @CircuitBreaker(name = WALLET_SERVICE, fallbackMethod = "debitFallback")
    public boolean debit(UUID walletId, long amountMinor, Currency currency, String referenceId) {
        log.info("[WalletClient] DEBIT walletId={} amount={} {} ref={}",
                walletId, amountMinor, currency, referenceId);
        // Production: return restTemplate.postForObject(walletServiceUrl + "/api/v1/wallets/{id}/debit", ...)
        return true;
    }

    @Override
    @Bulkhead(name = WALLET_SERVICE, type = Bulkhead.Type.SEMAPHORE,
              fallbackMethod = "creditFallback")
    @Retry(name = WALLET_SERVICE, fallbackMethod = "creditFallback")
    @CircuitBreaker(name = WALLET_SERVICE, fallbackMethod = "creditFallback")
    public boolean credit(UUID walletId, long amountMinor, Currency currency, String referenceId) {
        log.info("[WalletClient] CREDIT walletId={} amount={} {} ref={}",
                walletId, amountMinor, currency, referenceId);
        // Production: return restTemplate.postForObject(walletServiceUrl + "/api/v1/wallets/{id}/credit", ...)
        return true;
    }

    // ==========================================
    //  Fallbacks — invoked when CB is OPEN or retries exhausted
    // ==========================================

    /**
     * Debit fallback: returns false so the Saga orchestrator marks DEBIT_SENDER as failed
     * and terminates without compensation (sender was never debited).
     */
    public boolean debitFallback(UUID walletId, long amountMinor, Currency currency, String referenceId, Throwable ex) {
        log.error("[WalletClient FALLBACK] DEBIT failed for walletId={} ref={}: {} - CB={}",
                walletId, referenceId, ex.getClass().getSimpleName(), ex.getMessage());
        return false;
    }

    /**
     * Credit fallback: returns false so the Saga orchestrator triggers backward compensation
     * (refunds the already-debited sender).
     */
    public boolean creditFallback(UUID walletId, long amountMinor, Currency currency, String referenceId, Throwable ex) {
        log.error("[WalletClient FALLBACK] CREDIT failed for walletId={} ref={}: {} - triggering saga compensation",
                walletId, referenceId, ex.getMessage());
        return false;
    }
}
