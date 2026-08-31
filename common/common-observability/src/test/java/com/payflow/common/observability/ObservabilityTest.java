package com.payflow.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Observability Infrastructure Tests")
class ObservabilityTest {

    private MeterRegistry registry;
    private PayFlowMetrics metrics;

    @BeforeEach
    void setUp() {
        // SimpleMeterRegistry is an in-memory registry — no Prometheus server needed in tests
        registry = new SimpleMeterRegistry();
        metrics = new PayFlowMetrics(registry);
    }

    // ==========================================
    //  MDC / Trace Propagation Tests
    // ==========================================

    @Test
    @DisplayName("MdcConstants should define all required context keys")
    void shouldDefineAllMdcKeys() {
        assertThat(MdcConstants.TRACE_ID).isEqualTo("traceId");
        assertThat(MdcConstants.SPAN_ID).isEqualTo("spanId");
        assertThat(MdcConstants.USER_ID).isEqualTo("userId");
        assertThat(MdcConstants.TRANSACTION_ID).isEqualTo("transactionId");
        assertThat(MdcConstants.TRACE_HEADER).isEqualTo("X-Trace-Id");
    }

    @Test
    @DisplayName("MDC context should be isolated between operations")
    void mdcContextShouldBeIsolated() {
        MDC.put(MdcConstants.TRACE_ID, "trace-001");
        MDC.put(MdcConstants.USER_ID, "user-001");
        assertThat(MDC.get(MdcConstants.TRACE_ID)).isEqualTo("trace-001");

        // Simulate filter cleanup
        MDC.remove(MdcConstants.TRACE_ID);
        MDC.remove(MdcConstants.USER_ID);

        assertThat(MDC.get(MdcConstants.TRACE_ID)).isNull();
        assertThat(MDC.get(MdcConstants.USER_ID)).isNull();
    }

    // ==========================================
    //  MetricNames Registry Tests
    // ==========================================

    @Test
    @DisplayName("MetricNames should follow payflow.<noun>.<verb> naming convention")
    void shouldFollowNamingConvention() {
        assertThat(MetricNames.PAYMENT_INITIATED).startsWith("payflow.");
        assertThat(MetricNames.FRAUD_EVALUATION).startsWith("payflow.");
        assertThat(MetricNames.SAGA_COMPENSATED).startsWith("payflow.");
        assertThat(MetricNames.NOTIFICATION_SENT).startsWith("payflow.");
    }

    // ==========================================
    //  PayFlowMetrics Counter Tests
    // ==========================================

    @Test
    @DisplayName("incrementPaymentInitiated should register a counter with currency tag")
    void shouldRegisterPaymentInitiatedCounter() {
        metrics.incrementPaymentInitiated("INR");
        metrics.incrementPaymentInitiated("INR");

        double count = registry.counter(
                MetricNames.PAYMENT_INITIATED,
                MetricNames.TAG_CURRENCY, "INR"
        ).count();

        assertThat(count).isEqualTo(2.0);
    }

    @Test
    @DisplayName("incrementPaymentCompleted and incrementPaymentFailed should be independent counters")
    void completedAndFailedShouldBeIndependentCounters() {
        metrics.incrementPaymentCompleted("USD");
        metrics.incrementPaymentCompleted("USD");
        metrics.incrementPaymentFailed("USD");

        assertThat(registry.counter(MetricNames.PAYMENT_COMPLETED, MetricNames.TAG_CURRENCY, "USD").count()).isEqualTo(2.0);
        assertThat(registry.counter(MetricNames.PAYMENT_FAILED, MetricNames.TAG_CURRENCY, "USD").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("incrementFraudEvaluation should tag by decision (APPROVED / REVIEW / REJECTED)")
    void shouldTagFraudEvaluationByDecision() {
        metrics.incrementFraudEvaluation("APPROVED");
        metrics.incrementFraudEvaluation("APPROVED");
        metrics.incrementFraudEvaluation("REJECTED");

        assertThat(registry.counter(MetricNames.FRAUD_EVALUATION, MetricNames.TAG_DECISION, "APPROVED").count()).isEqualTo(2.0);
        assertThat(registry.counter(MetricNames.FRAUD_EVALUATION, MetricNames.TAG_DECISION, "REJECTED").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("incrementSagaCompensationFailed should be independently queryable for alerting")
    void shouldTrackSagaCompensationFailures() {
        metrics.incrementSagaCompensationFailed();
        metrics.incrementSagaCompensationFailed();

        double count = registry.counter(MetricNames.SAGA_COMPENSATION_FAILED).count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    @DisplayName("incrementNotificationSent should tag by channel")
    void shouldTagNotificationsByChannel() {
        metrics.incrementNotificationSent("EMAIL");
        metrics.incrementNotificationSent("SMS");
        metrics.incrementNotificationSent("EMAIL");

        assertThat(registry.counter(MetricNames.NOTIFICATION_SENT, MetricNames.TAG_CHANNEL, "EMAIL").count()).isEqualTo(2.0);
        assertThat(registry.counter(MetricNames.NOTIFICATION_SENT, MetricNames.TAG_CHANNEL, "SMS").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("recordPaymentProcessingTime should register a Timer in the registry")
    void shouldRegisterPaymentProcessingTimer() {
        metrics.recordPaymentProcessingTime(250, "COMPLETED", "INR");
        metrics.recordPaymentProcessingTime(480, "COMPLETED", "INR");
        metrics.recordPaymentProcessingTime(1200, "FAILED", "INR");

        var completedTimer = registry.find(MetricNames.PAYMENT_PROCESSING_TIME)
                .tag(MetricNames.TAG_STATUS, "COMPLETED")
                .tag(MetricNames.TAG_CURRENCY, "INR")
                .timer();

        assertThat(completedTimer).isNotNull();
        assertThat(completedTimer.count()).isEqualTo(2);
    }
}
