package com.payflow.merchant.dto;

import com.payflow.merchant.domain.entity.Merchant;
import com.payflow.merchant.domain.enums.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

public record MerchantResponse(
        UUID id,
        UUID userId,
        String businessName,
        String businessType,
        MerchantStatus status,
        String settlementBankAccount,
        Instant createdAt
) {
    public static MerchantResponse fromEntity(Merchant m) {
        return new MerchantResponse(
                m.getId(),
                m.getUserId(),
                m.getBusinessName(),
                m.getBusinessType(),
                m.getStatus(),
                m.getSettlementBankAccount(),
                m.getCreatedAt()
        );
    }
}
