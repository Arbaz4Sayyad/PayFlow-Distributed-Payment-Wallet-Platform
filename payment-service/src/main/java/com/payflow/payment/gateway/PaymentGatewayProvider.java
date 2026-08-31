package com.payflow.payment.gateway;

public interface PaymentGatewayProvider {

    GatewayResponse execute(GatewayRequest request);
}
