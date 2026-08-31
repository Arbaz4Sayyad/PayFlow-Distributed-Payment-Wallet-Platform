package com.payflow.merchant.service;

import com.payflow.common.model.exception.PayFlowException;
import com.payflow.merchant.domain.entity.Merchant;
import com.payflow.merchant.domain.entity.MerchantApiKey;
import com.payflow.merchant.domain.enums.MerchantStatus;
import com.payflow.merchant.domain.repository.MerchantApiKeyRepository;
import com.payflow.merchant.domain.repository.MerchantRepository;
import com.payflow.merchant.dto.ApiKeyResponse;
import com.payflow.merchant.dto.CreateApiKeyRequest;
import com.payflow.merchant.dto.MerchantResponse;
import com.payflow.merchant.dto.RegisterMerchantRequest;
import com.payflow.merchant.security.ApiKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository merchantRepository;
    private final MerchantApiKeyRepository apiKeyRepository;

    public MerchantService(
            MerchantRepository merchantRepository,
            MerchantApiKeyRepository apiKeyRepository
    ) {
        this.merchantRepository = merchantRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional
    public MerchantResponse registerMerchant(RegisterMerchantRequest request) {
        if (merchantRepository.existsByUserId(request.userId())) {
            throw new PayFlowException("MERCHANT_ALREADY_EXISTS",
                    "A merchant account already exists for user " + request.userId(), 409);
        }

        Merchant merchant = new Merchant(request.userId(), request.businessName(), request.businessType());
        merchant = merchantRepository.save(merchant);

        log.info("Merchant {} registered: {} ({})", merchant.getId(), merchant.getBusinessName(), merchant.getBusinessType());
        return MerchantResponse.fromEntity(merchant);
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PayFlowException("MERCHANT_NOT_FOUND",
                        "Merchant not found: " + merchantId, 404));
        return MerchantResponse.fromEntity(merchant);
    }

    @Transactional
    public MerchantResponse suspendMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PayFlowException("MERCHANT_NOT_FOUND",
                        "Merchant not found: " + merchantId, 404));
        merchant.suspend("Administrative action");
        merchantRepository.save(merchant);
        log.info("Merchant {} suspended", merchantId);
        return MerchantResponse.fromEntity(merchant);
    }

    @Transactional
    public MerchantResponse reactivateMerchant(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new PayFlowException("MERCHANT_NOT_FOUND",
                        "Merchant not found: " + merchantId, 404));
        merchant.reactivate();
        merchantRepository.save(merchant);
        log.info("Merchant {} reactivated", merchantId);
        return MerchantResponse.fromEntity(merchant);
    }

    // ==========================================
    //  API Key Management
    // ==========================================

    /**
     * Generates a new raw API key, stores only its SHA-256 hash, and returns
     * the raw key to the caller ONE TIME. Subsequent lookups never expose the raw key.
     */
    @Transactional
    public ApiKeyResponse createApiKey(UUID merchantId, CreateApiKeyRequest request) {
        Merchant merchant = merchantRepository.findByIdAndStatus(merchantId, MerchantStatus.ACTIVE)
                .orElseThrow(() -> new PayFlowException("MERCHANT_INACTIVE",
                        "Only ACTIVE merchants can generate API keys", 422));

        String rawKey = ApiKeyUtil.generateRawKey();
        String keyHash = ApiKeyUtil.hashKey(rawKey);

        MerchantApiKey apiKey = new MerchantApiKey(merchant, keyHash, request.label());
        apiKey = apiKeyRepository.save(apiKey);

        log.info("API key [{}] created for merchant {}", apiKey.getId(), merchantId);
        return ApiKeyResponse.fromEntityWithRawKey(apiKey, rawKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listApiKeys(UUID merchantId) {
        return apiKeyRepository.findByMerchantId(merchantId).stream()
                .map(ApiKeyResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void revokeApiKey(UUID merchantId, UUID keyId) {
        MerchantApiKey apiKey = apiKeyRepository.findByIdAndMerchantId(keyId, merchantId)
                .orElseThrow(() -> new PayFlowException("API_KEY_NOT_FOUND",
                        "API key not found: " + keyId, 404));

        if (apiKey.isRevoked()) {
            throw new PayFlowException("API_KEY_ALREADY_REVOKED",
                    "This API key has already been revoked", 409);
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
        log.info("API key {} revoked for merchant {}", keyId, merchantId);
    }
}
