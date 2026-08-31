package com.payflow.merchant.security;

import com.payflow.merchant.domain.entity.MerchantApiKey;
import com.payflow.merchant.domain.repository.MerchantApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validates incoming Bearer API keys against the hashed keys in the database.
 * Used by the Spring Security filter chain for merchant API key authentication.
 */
@Component
public class ApiKeyAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticator.class);

    private final MerchantApiKeyRepository apiKeyRepository;

    public ApiKeyAuthenticator(MerchantApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    /**
     * Validates a raw API key by:
     * 1. Computing SHA-256(rawKey)
     * 2. Looking up the hash in merchant_api_keys (non-revoked only)
     * 3. Returning a UserDetails representing the merchant principal
     *
     * @throws BadCredentialsException if key is invalid or revoked
     */
    public UserDetails authenticate(String rawKey) {
        if (rawKey == null || !rawKey.startsWith(ApiKeyUtil.KEY_PREFIX)) {
            throw new BadCredentialsException("Invalid API key format");
        }

        String keyHash = ApiKeyUtil.hashKey(rawKey);
        MerchantApiKey apiKey = apiKeyRepository.findByApiKeyHashAndRevokedFalse(keyHash)
                .orElseThrow(() -> {
                    log.warn("API key authentication failed: no active key found for hash prefix");
                    return new BadCredentialsException("Invalid or revoked API key");
                });

        log.debug("API key authenticated for merchant {}, label: {}",
                apiKey.getMerchant().getId(), apiKey.getLabel());

        return User.builder()
                .username("merchant:" + apiKey.getMerchant().getId())
                .password("[PROTECTED]")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")))
                .build();
    }
}
