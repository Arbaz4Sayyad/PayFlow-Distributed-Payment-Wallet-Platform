package com.payflow.fraud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.fraud.domain.entity.FlaggedTransaction;
import com.payflow.fraud.domain.enums.FraudDecision;
import com.payflow.fraud.domain.repository.BlacklistRecordRepository;
import com.payflow.fraud.domain.repository.FlaggedTransactionRepository;
import com.payflow.fraud.dto.FraudEvaluationRequest;
import com.payflow.fraud.dto.FraudEvaluationResponse;
import com.payflow.fraud.rule.FraudRule;
import com.payflow.fraud.tracker.VelocityTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudEvaluationService Unit Tests")
class FraudEvaluationServiceTest {

    @Mock
    private FlaggedTransactionRepository flaggedTransactionRepository;

    @Mock
    private BlacklistRecordRepository blacklistRecordRepository;

    @Mock
    private VelocityTracker velocityTracker;

    @Mock
    private FraudRule rule1;

    @Mock
    private FraudRule rule2;

    private FraudEvaluationService fraudEvaluationService;

    @BeforeEach
    void setUp() {
        fraudEvaluationService = new FraudEvaluationService(
                List.of(rule1, rule2),
                flaggedTransactionRepository,
                blacklistRecordRepository,
                velocityTracker,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("Should APPROVE transaction with low risk score (< 30)")
    void shouldApproveLowRiskTransaction() {
        when(rule1.evaluate(any())).thenReturn(0);
        when(rule2.evaluate(any())).thenReturn(10);

        FraudEvaluationRequest request = new FraudEvaluationRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("50.00"), Currency.INR, "127.0.0.1", "DEV-1"
        );

        FraudEvaluationResponse response = fraudEvaluationService.evaluate(request);

        assertThat(response.decision()).isEqualTo(FraudDecision.APPROVED);
        assertThat(response.riskScore()).isEqualTo(10);
        verify(flaggedTransactionRepository, never()).save(any(FlaggedTransaction.class));
        verify(velocityTracker).recordTransaction(request.userId(), 5000L);
    }

    @Test
    @DisplayName("Should flag transaction for REVIEW with medium risk score (30-69)")
    void shouldFlagMediumRiskTransactionForReview() {
        when(rule1.evaluate(any())).thenReturn(30);
        when(rule1.getName()).thenReturn("HIGH_AMOUNT");
        when(rule2.evaluate(any())).thenReturn(10);
        when(rule2.getName()).thenReturn("VELOCITY");

        FraudEvaluationRequest request = new FraudEvaluationRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("1500.00"), Currency.INR, "127.0.0.1", "DEV-1"
        );

        FraudEvaluationResponse response = fraudEvaluationService.evaluate(request);

        assertThat(response.decision()).isEqualTo(FraudDecision.REVIEW);
        assertThat(response.riskScore()).isEqualTo(40);
        verify(flaggedTransactionRepository).save(any(FlaggedTransaction.class));
    }

    @Test
    @DisplayName("Should REJECT high risk transaction (>= 70)")
    void shouldRejectHighRiskTransaction() {
        when(rule1.evaluate(any())).thenReturn(100);
        when(rule1.getName()).thenReturn("BLACKLIST_MATCH");

        FraudEvaluationRequest request = new FraudEvaluationRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("500.00"), Currency.INR, "10.0.0.1", "DEV-BLACK"
        );

        FraudEvaluationResponse response = fraudEvaluationService.evaluate(request);

        assertThat(response.decision()).isEqualTo(FraudDecision.REJECTED);
        assertThat(response.riskScore()).isEqualTo(100);
        verify(flaggedTransactionRepository).save(any(FlaggedTransaction.class));
    }
}
