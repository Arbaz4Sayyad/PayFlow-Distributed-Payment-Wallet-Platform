package com.payflow.merchant.dto;

import com.payflow.merchant.domain.entity.MerchantApiKey;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        UUID merchantId,
        String label,
        boolean revoked,
        Instant createdAt,
        /**
         * Raw key is populated ONLY on creation — null on all subsequent reads.
         * This is the single time the caller can retrieve the plaintext key.
         */
        String rawKey
) {
    public static ApiKeyResponse fromEntity(MerchantApiKey key) {
        return new ApiKeyResponse(
                key.getId(),
                key.getMerchant().getId(),
                key.getLabel(),
                key.isRevoked(),
                key.getCreatedAt(),
                null // Raw key never returned on lookup
        );
    }

    public static ApiKeyResponse fromEntityWithRawKey(MerchantApiKey key, String rawKey) {
        return new ApiKeyResponse(
                key.getId(),
                key.getMerchant().getId(),
                key.getLabel(),
                key.isRevoked(),
                key.getCreatedAt(),
                rawKey // Shown ONCE at creation time only
        );
    }
}
