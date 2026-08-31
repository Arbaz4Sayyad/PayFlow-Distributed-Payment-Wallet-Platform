package com.payflow.merchant.domain.repository;

import com.payflow.merchant.domain.entity.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {

    /**
     * Lookup by SHA-256 hash of the raw API key — used for request authentication.
     * Filtered to active (non-revoked) keys only via partial index in schema.
     */
    Optional<MerchantApiKey> findByApiKeyHashAndRevokedFalse(String apiKeyHash);

    List<MerchantApiKey> findByMerchantId(UUID merchantId);

    Optional<MerchantApiKey> findByIdAndMerchantId(UUID id, UUID merchantId);
}
