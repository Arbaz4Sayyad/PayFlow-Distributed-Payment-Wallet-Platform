package com.payflow.fraud.rule;

import com.payflow.fraud.domain.repository.BlacklistRecordRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class BlacklistRule implements FraudRule {

    private final BlacklistRecordRepository blacklistRecordRepository;

    public BlacklistRule(BlacklistRecordRepository blacklistRecordRepository) {
        this.blacklistRecordRepository = blacklistRecordRepository;
    }

    @Override
    public String getName() {
        return "BLACKLIST_MATCH";
    }

    @Override
    public int evaluate(FraudEvaluationContext context) {
        if (context.userId() != null &&
                blacklistRecordRepository.existsByTargetTypeAndTargetValue("USER", context.userId().toString())) {
            return 100;
        }

        if (context.walletId() != null &&
                blacklistRecordRepository.existsByTargetTypeAndTargetValue("WALLET", context.walletId().toString())) {
            return 100;
        }

        if (context.ipAddress() != null &&
                blacklistRecordRepository.existsByTargetTypeAndTargetValue("IP", context.ipAddress())) {
            return 100;
        }

        return 0;
    }
}
