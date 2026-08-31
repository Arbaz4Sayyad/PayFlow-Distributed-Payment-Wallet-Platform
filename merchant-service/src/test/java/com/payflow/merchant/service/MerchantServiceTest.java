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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantService Unit Tests")
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantApiKeyRepository apiKeyRepository;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    @DisplayName("Should register a new merchant successfully")
    void shouldRegisterMerchantSuccessfully() {
        UUID userId = UUID.randomUUID();
        RegisterMerchantRequest request = new RegisterMerchantRequest(
                userId, "Acme Store", "RETAIL", null, null
        );

        when(merchantRepository.existsByUserId(userId)).thenReturn(false);
        when(merchantRepository.save(any(Merchant.class))).thenAnswer(inv -> inv.getArgument(0));

        MerchantResponse response = merchantService.registerMerchant(request);

        assertThat(response.businessName()).isEqualTo("Acme Store");
        assertThat(response.status()).isEqualTo(MerchantStatus.ACTIVE);
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should throw MERCHANT_ALREADY_EXISTS if user already has a merchant account")
    void shouldRejectDuplicateMerchantRegistration() {
        UUID userId = UUID.randomUUID();
        RegisterMerchantRequest request = new RegisterMerchantRequest(
                userId, "Duplicate Store", "RETAIL", null, null
        );

        when(merchantRepository.existsByUserId(userId)).thenReturn(true);

        assertThatThrownBy(() -> merchantService.registerMerchant(request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("already exists");

        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create API key and return raw key only at creation time")
    void shouldCreateApiKeyAndReturnRawKeyOnce() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant(UUID.randomUUID(), "Acme", "RETAIL");
        CreateApiKeyRequest request = new CreateApiKeyRequest("Production Key");

        when(merchantRepository.findByIdAndStatus(merchantId, MerchantStatus.ACTIVE))
                .thenReturn(Optional.of(merchant));
        when(apiKeyRepository.save(any(MerchantApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        ApiKeyResponse response = merchantService.createApiKey(merchantId, request);

        assertThat(response.rawKey()).isNotNull();
        assertThat(response.rawKey()).startsWith("pf_live_");
        assertThat(response.label()).isEqualTo("Production Key");
        assertThat(response.revoked()).isFalse();

        // Verify the stored hash is SHA-256 of the raw key
        ArgumentCaptor<MerchantApiKey> captor = ArgumentCaptor.forClass(MerchantApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        assertThat(captor.getValue().getApiKeyHash())
                .isEqualTo(ApiKeyUtil.hashKey(response.rawKey()));
    }

    @Test
    @DisplayName("Should reject API key creation for suspended merchant")
    void shouldRejectApiKeyCreationForSuspendedMerchant() {
        UUID merchantId = UUID.randomUUID();
        CreateApiKeyRequest request = new CreateApiKeyRequest("Test Key");

        when(merchantRepository.findByIdAndStatus(merchantId, MerchantStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> merchantService.createApiKey(merchantId, request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("ACTIVE merchants");
    }

    @Test
    @DisplayName("Should revoke an active API key")
    void shouldRevokeApiKey() {
        UUID merchantId = UUID.randomUUID();
        UUID keyId = UUID.randomUUID();
        Merchant merchant = new Merchant(UUID.randomUUID(), "Acme", "RETAIL");
        MerchantApiKey apiKey = new MerchantApiKey(merchant, "fakehash", "Test Key");

        when(apiKeyRepository.findByIdAndMerchantId(keyId, merchantId))
                .thenReturn(Optional.of(apiKey));
        when(apiKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        merchantService.revokeApiKey(merchantId, keyId);

        assertThat(apiKey.isRevoked()).isTrue();
        verify(apiKeyRepository).save(apiKey);
    }

    @Test
    @DisplayName("Should throw INVALID_STATE_TRANSITION when suspending an already-suspended merchant")
    void shouldThrowWhenSuspendingAlreadySuspendedMerchant() {
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant(UUID.randomUUID(), "Acme", "RETAIL");
        merchant.suspend("First suspension");

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        assertThatThrownBy(() -> merchantService.suspendMerchant(merchantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only ACTIVE merchants");
    }
}
