package com.payflow.common.model.event;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String WALLET_DEBITED = "wallet.debited";
    public static final String WALLET_CREDITED = "wallet.credited";

    public static final String LEDGER_RECORDED = "ledger.entry.recorded";

    // Dead Letter Topic suffix
    public static final String DLT_SUFFIX = ".dlt";
}
