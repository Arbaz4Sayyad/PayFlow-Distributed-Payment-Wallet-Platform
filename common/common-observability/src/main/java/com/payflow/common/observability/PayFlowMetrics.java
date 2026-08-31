package com.payflow.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Centralized facade for all PayFlow business metrics.
 *
 * Usage in services:
 *   payflowMetrics.incrementPaymentInitiated("INR");
 *   payflowMetrics.recordPaymentProcessingTime(durationMs, "COMPLETED", "INR");
 *
 * Metrics are automatically scraped by Prometheus at /actuator/prometheus
 * and visualised in Grafana dashboards.
 */
@Component
public class PayFlowMetrics {

    private final MeterRegistry registry;

    public PayFlowMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    // ==========================================
    //  Payment Counters
    // ==========================================

    public void incrementPaymentInitiated(String currency) {
        Counter.builder(MetricNames.PAYMENT_INITIATED)
                .description("Total payment initiations")
                .tag(MetricNames.TAG_CURRENCY, currency)
                .register(registry)
                .increment();
    }

    public void incrementPaymentCompleted(String currency) {
        Counter.builder(MetricNames.PAYMENT_COMPLETED)
                .description("Total payment completions")
                .tag(MetricNames.TAG_CURRENCY, currency)
                .register(registry)
                .increment();
    }

    public void incrementPaymentFailed(String currency) {
        Counter.builder(MetricNames.PAYMENT_FAILED)
                .description("Total payment failures")
                .tag(MetricNames.TAG_CURRENCY, currency)
                .register(registry)
                .increment();
    }

    public void incrementPaymentRefunded(String currency) {
        Counter.builder(MetricNames.PAYMENT_REFUNDED)
                .description("Total payment refunds")
                .tag(MetricNames.TAG_CURRENCY, currency)
                .register(registry)
                .increment();
    }

    public void incrementIdempotentReplay() {
        Counter.builder(MetricNames.PAYMENT_IDEMPOTENT_REPLAY)
                .description("Duplicate payment requests detected via idempotency key")
                .register(registry)
                .increment();
    }

    /**
     * Records end-to-end payment processing latency.
     *
     * @param durationMs time in milliseconds from payment initiation to terminal state
     * @param status     terminal status (COMPLETED / FAILED)
     * @param currency   payment currency code
     */
    public void recordPaymentProcessingTime(long durationMs, String status, String currency) {
        Timer.builder(MetricNames.PAYMENT_PROCESSING_TIME)
                .description("End-to-end payment processing latency")
                .tag(MetricNames.TAG_STATUS, status)
                .tag(MetricNames.TAG_CURRENCY, currency)
                .publishPercentiles(0.5, 0.95, 0.99)    // p50, p95, p99
                .publishPercentileHistogram()
                .sla(
                    Duration.ofMillis(200),
                    Duration.ofMillis(500),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(2)
                )
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // ==========================================
    //  Fraud Counters
    // ==========================================

    public void incrementFraudEvaluation(String decision) {
        Counter.builder(MetricNames.FRAUD_EVALUATION)
                .description("Fraud evaluation decisions")
                .tag(MetricNames.TAG_DECISION, decision)
                .register(registry)
                .increment();
    }

    public void incrementFraudVelocityTrigger() {
        Counter.builder(MetricNames.FRAUD_VELOCITY_TRIGGER)
                .description("Velocity rule trigger events")
                .register(registry)
                .increment();
    }

    public void incrementFraudBlacklistHit() {
        Counter.builder(MetricNames.FRAUD_BLACKLIST_HIT)
                .description("Blacklist rule hit events")
                .register(registry)
                .increment();
    }

    // ==========================================
    //  Saga Counters
    // ==========================================

    public void incrementSagaStarted() {
        Counter.builder(MetricNames.SAGA_STARTED)
                .description("Transfer saga instances started")
                .register(registry)
                .increment();
    }

    public void incrementSagaCompleted() {
        Counter.builder(MetricNames.SAGA_COMPLETED)
                .description("Transfer saga instances completed successfully")
                .register(registry)
                .increment();
    }

    public void incrementSagaCompensated() {
        Counter.builder(MetricNames.SAGA_COMPENSATED)
                .description("Transfer saga backward compensations triggered")
                .register(registry)
                .increment();
    }

    public void incrementSagaCompensationFailed() {
        Counter.builder(MetricNames.SAGA_COMPENSATION_FAILED)
                .description("CRITICAL: saga compensation itself failed — requires manual reconciliation")
                .register(registry)
                .increment();
    }

    // ==========================================
    //  Notification Counters
    // ==========================================

    public void incrementNotificationSent(String channel) {
        Counter.builder(MetricNames.NOTIFICATION_SENT)
                .description("Notifications dispatched by channel")
                .tag(MetricNames.TAG_CHANNEL, channel)
                .register(registry)
                .increment();
    }

    public void incrementNotificationFailed(String channel) {
        Counter.builder(MetricNames.NOTIFICATION_FAILED)
                .description("Notification delivery failures by channel")
                .tag(MetricNames.TAG_CHANNEL, channel)
                .register(registry)
                .increment();
    }

    // ==========================================
    //  Resilience Counters
    // ==========================================

    public void incrementCircuitBreakerFallback(String serviceName) {
        Counter.builder(MetricNames.CB_FALLBACK_INVOKED)
                .description("Circuit breaker fallback invocations")
                .tag(MetricNames.TAG_SERVICE, serviceName)
                .register(registry)
                .increment();
    }
}
