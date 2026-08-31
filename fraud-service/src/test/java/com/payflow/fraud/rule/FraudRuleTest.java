package com.payflow.fraud.rule;

import com.payflow.common.model.currency.Currency;
import com.payflow.fraud.domain.repository.BlacklistRecordRepository;
import com.payflow.fraud.tracker.VelocityTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Fraud Rules Unit Tests")
class FraudRuleTest {

    @Mock
    private BlacklistRecordRepository blacklistRecordRepository;

    @Mock
    private VelocityTracker velocityTracker;

    @Test
    @DisplayName("BlacklistRule: Should return 100 penalty points for blacklisted user")
    void shouldFlagBlacklistedUser() {
        UUID userId = UUID.randomUUID();
        when(blacklistRecordRepository.existsByTargetTypeAndTargetValue("USER", userId.toString())).thenReturn(true);

        BlacklistRule rule = new BlacklistRule(blacklistRecordRepository);
        FraudEvaluationContext context = new FraudEvaluationContext(
                UUID.randomUUID(), userId, UUID.randomUUID(), 5000L, Currency.INR, "192.168.1.1", "DEV-1"
        );

        assertThat(rule.evaluate(context)).isEqualTo(100);
    }

    @Test
    @DisplayName("HighAmountRule: Should assign 30 points for medium and 60 points for high amount")
    void shouldScoreHighAmounts() {
        HighAmountRule rule = new HighAmountRule();

        FraudEvaluationContext normal = new FraudEvaluationContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 50000L, Currency.INR, null, null);
        FraudEvaluationContext medium = new FraudEvaluationContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 150000L, Currency.INR, null, null);
        FraudEvaluationContext high = new FraudEvaluationContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 600000L, Currency.INR, null, null);

        assertThat(rule.evaluate(normal)).isEqualTo(0);
        assertThat(rule.evaluate(medium)).isEqualTo(30);
        assertThat(rule.evaluate(high)).isEqualTo(60);
    }

    @Test
    @DisplayName("TransactionVelocityRule: Should penalize high frequency bursts")
    void shouldDetectVelocityBurst() {
        UUID userId = UUID.randomUUID();
        when(velocityTracker.getTransactionCount(eq(userId), any(Duration.class))).thenReturn(6);

        TransactionVelocityRule rule = new TransactionVelocityRule(velocityTracker);
        FraudEvaluationContext context = new FraudEvaluationContext(
                UUID.randomUUID(), userId, UUID.randomUUID(), 1000L, Currency.INR, null, null
        );

        assertThat(rule.evaluate(context)).isEqualTo(50);
    }

    @Test
    @DisplayName("RapidMicroTransactionRule: Should detect micro-transaction card testing bots")
    void shouldDetectMicroTxBot() {
        UUID userId = UUID.randomUUID();
        when(velocityTracker.getTransactionCount(eq(userId), any(Duration.class))).thenReturn(4);

        RapidMicroTransactionRule rule = new RapidMicroTransactionRule(velocityTracker);
        FraudEvaluationContext context = new FraudEvaluationContext(
                UUID.randomUUID(), userId, UUID.randomUUID(), 50L, Currency.INR, null, null // 0.50 INR
        );

        assertThat(rule.evaluate(context)).isEqualTo(45);
    }
}
