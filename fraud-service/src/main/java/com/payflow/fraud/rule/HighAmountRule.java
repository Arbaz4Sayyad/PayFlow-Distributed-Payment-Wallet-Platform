package com.payflow.fraud.rule;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class HighAmountRule implements FraudRule {

    // Thresholds in minor units (e.g. 100000 minor = 1,000 INR / USD)
    private static final long MEDIUM_AMOUNT_THRESHOLD = 100000L;
    private static final long HIGH_AMOUNT_THRESHOLD = 500000L;

    @Override
    public String getName() {
        return "HIGH_AMOUNT_ANOMALY";
    }

    @Override
    public int evaluate(FraudEvaluationContext context) {
        if (context.amountMinor() >= HIGH_AMOUNT_THRESHOLD) {
            return 60;
        } else if (context.amountMinor() >= MEDIUM_AMOUNT_THRESHOLD) {
            return 30;
        }
        return 0;
    }
}
