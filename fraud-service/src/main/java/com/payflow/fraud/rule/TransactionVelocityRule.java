package com.payflow.fraud.rule;

import com.payflow.fraud.tracker.VelocityTracker;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Order(3)
public class TransactionVelocityRule implements FraudRule {

    private final VelocityTracker velocityTracker;

    public TransactionVelocityRule(VelocityTracker velocityTracker) {
        this.velocityTracker = velocityTracker;
    }

    @Override
    public String getName() {
        return "RAPID_VELOCITY_BURST";
    }

    @Override
    public int evaluate(FraudEvaluationContext context) {
        if (context.userId() == null) {
            return 0;
        }

        int countLastMinute = velocityTracker.getTransactionCount(context.userId(), Duration.ofMinutes(1));

        if (countLastMinute >= 10) {
            return 80;
        } else if (countLastMinute >= 5) {
            return 50;
        }
        return 0;
    }
}
