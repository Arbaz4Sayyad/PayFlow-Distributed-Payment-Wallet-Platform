package com.payflow.common.observability;

/**
 * Centralized Micrometer metric name registry.
 *
 * Naming convention: <service>.<noun>.<verb/state>
 * All metric names are lowercase with dots as separators (Prometheus converts dots to underscores).
 */
public final class MetricNames {

    private MetricNames() {
    }

    // ==========================================
    //  Payment Metrics
    // ==========================================
    /** Counter: total payment initiations */
    public static final String PAYMENT_INITIATED        = "payflow.payment.initiated";
    /** Counter: payment completions (final state) */
    public static final String PAYMENT_COMPLETED        = "payflow.payment.completed";
    /** Counter: payment failures (final state) */
    public static final String PAYMENT_FAILED           = "payflow.payment.failed";
    /** Counter: payment refunds */
    public static final String PAYMENT_REFUNDED         = "payflow.payment.refunded";
    /** Counter: idempotent replays (duplicate detection fired) */
    public static final String PAYMENT_IDEMPOTENT_REPLAY = "payflow.payment.idempotent.replay";
    /** Timer: end-to-end payment processing latency */
    public static final String PAYMENT_PROCESSING_TIME  = "payflow.payment.processing.duration";

    // ==========================================
    //  Wallet Metrics
    // ==========================================
    /** Counter: debit operations */
    public static final String WALLET_DEBIT             = "payflow.wallet.debit";
    /** Counter: credit operations */
    public static final String WALLET_CREDIT            = "payflow.wallet.credit";
    /** Counter: insufficient funds rejections */
    public static final String WALLET_INSUFFICIENT_FUNDS = "payflow.wallet.insufficient.funds";

    // ==========================================
    //  Fraud Metrics
    // ==========================================
    /** Counter: fraud evaluations by decision (APPROVED / REVIEW / REJECTED) */
    public static final String FRAUD_EVALUATION         = "payflow.fraud.evaluation";
    /** Counter: velocity rule triggers */
    public static final String FRAUD_VELOCITY_TRIGGER   = "payflow.fraud.velocity.trigger";
    /** Counter: blacklist hits */
    public static final String FRAUD_BLACKLIST_HIT      = "payflow.fraud.blacklist.hit";

    // ==========================================
    //  Saga Metrics
    // ==========================================
    /** Counter: saga started */
    public static final String SAGA_STARTED             = "payflow.saga.started";
    /** Counter: saga completed successfully */
    public static final String SAGA_COMPLETED           = "payflow.saga.completed";
    /** Counter: saga compensation triggered */
    public static final String SAGA_COMPENSATED         = "payflow.saga.compensated";
    /** Counter: saga compensation itself failed (CRITICAL) */
    public static final String SAGA_COMPENSATION_FAILED = "payflow.saga.compensation.failed";

    // ==========================================
    //  Notification Metrics
    // ==========================================
    /** Counter: notifications dispatched, tagged by channel (EMAIL/SMS/PUSH) */
    public static final String NOTIFICATION_SENT        = "payflow.notification.sent";
    /** Counter: notification delivery failures */
    public static final String NOTIFICATION_FAILED      = "payflow.notification.failed";

    // ==========================================
    //  Resilience Metrics (supplement Resilience4j auto-metrics)
    // ==========================================
    /** Counter: circuit breaker fallback invocations */
    public static final String CB_FALLBACK_INVOKED      = "payflow.circuitbreaker.fallback";
    /** Counter: retry attempts beyond first call */
    public static final String RETRY_ATTEMPT            = "payflow.retry.attempt";

    // ==========================================
    //  Common Tags
    // ==========================================
    public static final String TAG_STATUS    = "status";
    public static final String TAG_CHANNEL   = "channel";
    public static final String TAG_DECISION  = "decision";
    public static final String TAG_CURRENCY  = "currency";
    public static final String TAG_SERVICE   = "service";
}
