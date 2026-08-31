package com.payflow.fraud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Money;
import com.payflow.fraud.domain.entity.BlacklistRecord;
import com.payflow.fraud.domain.entity.FlaggedTransaction;
import com.payflow.fraud.domain.enums.FraudDecision;
import com.payflow.fraud.domain.repository.BlacklistRecordRepository;
import com.payflow.fraud.domain.repository.FlaggedTransactionRepository;
import com.payflow.fraud.dto.AddBlacklistRequest;
import com.payflow.fraud.dto.FraudEvaluationRequest;
import com.payflow.fraud.dto.FraudEvaluationResponse;
import com.payflow.fraud.rule.FraudEvaluationContext;
import com.payflow.fraud.rule.FraudRule;
import com.payflow.fraud.tracker.VelocityTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FraudEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(FraudEvaluationService.class);

    private final List<FraudRule> rules;
    private final FlaggedTransactionRepository flaggedTransactionRepository;
    private final BlacklistRecordRepository blacklistRecordRepository;
    private final VelocityTracker velocityTracker;
    private final ObjectMapper objectMapper;

    public FraudEvaluationService(
            List<FraudRule> rules,
            FlaggedTransactionRepository flaggedTransactionRepository,
            BlacklistRecordRepository blacklistRecordRepository,
            VelocityTracker velocityTracker,
            ObjectMapper objectMapper
    ) {
        this.rules = rules;
        this.flaggedTransactionRepository = flaggedTransactionRepository;
        this.blacklistRecordRepository = blacklistRecordRepository;
        this.velocityTracker = velocityTracker;
        this.objectMapper = objectMapper;
    }

    /**
     * Synchronously evaluates transaction risk score and produces a FraudDecision.
     */
    @Transactional
    public FraudEvaluationResponse evaluate(FraudEvaluationRequest request) {
        Money money = Money.fromBigDecimal(request.amount(), request.currency());
        FraudEvaluationContext context = new FraudEvaluationContext(
                request.transactionId(),
                request.userId(),
                request.walletId(),
                money.amountMinor(),
                request.currency(),
                request.ipAddress(),
                request.deviceId()
        );

        List<String> triggeredRuleNames = new ArrayList<>();
        int totalRiskScore = 0;

        for (FraudRule rule : rules) {
            int penalty = rule.evaluate(context);
            if (penalty > 0) {
                triggeredRuleNames.add(rule.getName() + " (+" + penalty + ")");
                totalRiskScore += penalty;
            }
        }

        // Cap risk score between 0 and 100
        totalRiskScore = Math.min(totalRiskScore, 100);

        FraudDecision decision;
        String summary;

        if (totalRiskScore >= 70) {
            decision = FraudDecision.REJECTED;
            summary = "High risk detected. Transaction automatically blocked.";
            log.warn("Transaction {} BLOCKED. Risk score: {}, Rules: {}",
                    request.transactionId(), totalRiskScore, triggeredRuleNames);
        } else if (totalRiskScore >= 30) {
            decision = FraudDecision.REVIEW;
            summary = "Medium risk detected. Transaction flagged for review.";
            log.info("Transaction {} flagged for REVIEW. Risk score: {}, Rules: {}",
                    request.transactionId(), totalRiskScore, triggeredRuleNames);
        } else {
            decision = FraudDecision.APPROVED;
            summary = "Low risk. Transaction approved.";
            log.debug("Transaction {} APPROVED. Risk score: {}", request.transactionId(), totalRiskScore);
        }

        // Record audit entry for all flagged or blocked transactions
        if (decision != FraudDecision.APPROVED) {
            String rulesJson;
            try {
                rulesJson = objectMapper.writeValueAsString(triggeredRuleNames);
            } catch (JsonProcessingException e) {
                rulesJson = "[]";
            }

            FlaggedTransaction flagged = new FlaggedTransaction(
                    request.transactionId(),
                    request.userId(),
                    totalRiskScore,
                    decision,
                    rulesJson
            );
            flaggedTransactionRepository.save(flagged);
        }

        // Update velocity history
        velocityTracker.recordTransaction(request.userId(), money.amountMinor());

        return FraudEvaluationResponse.of(
                request.transactionId(),
                totalRiskScore,
                decision,
                triggeredRuleNames,
                summary
        );
    }

    @Transactional
    public BlacklistRecord addToBlacklist(AddBlacklistRequest request) {
        BlacklistRecord record = new BlacklistRecord(
                request.targetType(),
                request.targetValue(),
                request.reason()
        );
        record = blacklistRecordRepository.save(record);
        log.info("Added to blacklist: [{}] {} - Reason: {}",
                request.targetType(), request.targetValue(), request.reason());
        return record;
    }
}
