package com.payflow.common.observability;

public final class MdcConstants {

    private MdcConstants() {
    }

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String USER_ID = "userId";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRACE_HEADER = "X-Trace-Id";
}
