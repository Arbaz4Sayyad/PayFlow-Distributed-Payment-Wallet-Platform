package com.payflow.payment.gateway;

public record GatewayResponse(
        boolean success,
        String gatewayTransactionId,
        String errorCode,
        String errorMessage
) {
    public static GatewayResponse approved(String gatewayTxId) {
        return new GatewayResponse(true, gatewayTxId, null, null);
    }

    public static GatewayResponse declined(String errorCode, String errorMessage) {
        return new GatewayResponse(false, null, errorCode, errorMessage);
    }
}
