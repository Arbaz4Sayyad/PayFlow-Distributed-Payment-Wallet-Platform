package com.payflow.fraud.dto;

import com.payflow.fraud.domain.enums.FraudDecision;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudEvaluationResponse(
        UUID transactionId,
        int riskScore,
        FraudDecision decision,
        List<String> triggeredRules,
        String summary,
        Instant evaluatedAt
) {
    public static FraudEvaluationResponse of(
            UUID transactionId,
            int riskScore,
            FraudDecision decision,
            List<String> triggeredRules,
            String summary
    ) {
        return new FraudEvaluationResponse(
                transactionId,
                riskScore,
                decision,
                triggeredRules,
                summary,
                Instant.now()
        );
    }
}
