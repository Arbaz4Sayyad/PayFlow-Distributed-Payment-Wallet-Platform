package com.payflow.fraud.rule;

import com.payflow.fraud.tracker.VelocityTracker;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Order(4)
public class RapidMicroTransactionRule implements FraudRule {

    private static final long MICRO_TX_LIMIT_MINOR = 100L; // 100 minor units (1.00 INR / USD)
    private final VelocityTracker velocityTracker;

    public RapidMicroTransactionRule(VelocityTracker velocityTracker) {
        this.velocityTracker = velocityTracker;
    }

    @Override
    public String getName() {
        return "CARD_TESTING_MICRO_TX";
    }

    @Override
    public int evaluate(FraudEvaluationContext context) {
        if (context.userId() == null) {
            return 0;
        }

        // Detect account/card testing: multiple tiny micro-transactions within 60 seconds
        if (context.amountMinor() <= MICRO_TX_LIMIT_MINOR) {
            int recentTxCount = velocityTracker.getTransactionCount(context.userId(), Duration.ofMinutes(1));
            if (recentTxCount >= 3) {
                return 45; // Suspicious bot/card testing pattern
            }
        }
        return 0;
    }
}
