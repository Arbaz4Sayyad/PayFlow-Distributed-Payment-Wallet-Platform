package com.payflow.payment.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGatewayProvider implements PaymentGatewayProvider {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayProvider.class);

    @Override
    public GatewayResponse execute(GatewayRequest request) {
        log.info("Simulating external payment gateway invocation for payment: {}, amount: {} minor",
                request.paymentId(), request.amountMinor());

        // Test trigger: specific amounts can simulate decline
        if (request.amountMinor() == 99999999L) {
            log.warn("Mock Gateway: Simulating decline for test trigger amount 99999999");
            return GatewayResponse.declined("CARD_DECLINED", "Card has been declined by the issuing bank");
        }

        String gatewayTxId = "PGW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Mock Gateway: Payment approved. GatewayTxId: {}", gatewayTxId);
        return GatewayResponse.approved(gatewayTxId);
    }
}
