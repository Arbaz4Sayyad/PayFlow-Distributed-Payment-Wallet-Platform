package com.payflow.merchant.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.merchant.dto.ApiKeyResponse;
import com.payflow.merchant.dto.CreateApiKeyRequest;
import com.payflow.merchant.dto.MerchantResponse;
import com.payflow.merchant.dto.RegisterMerchantRequest;
import com.payflow.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    // ==========================================
    //  Merchant Lifecycle
    // ==========================================

    @PostMapping
    public ResponseEntity<ApiResponse<MerchantResponse>> registerMerchant(
            @Valid @RequestBody RegisterMerchantRequest request
    ) {
        MerchantResponse response = merchantService.registerMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<ApiResponse<MerchantResponse>> getMerchant(@PathVariable UUID merchantId) {
        MerchantResponse response = merchantService.getMerchant(merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{merchantId}/suspend")
    public ResponseEntity<ApiResponse<MerchantResponse>> suspendMerchant(@PathVariable UUID merchantId) {
        MerchantResponse response = merchantService.suspendMerchant(merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{merchantId}/reactivate")
    public ResponseEntity<ApiResponse<MerchantResponse>> reactivateMerchant(@PathVariable UUID merchantId) {
        MerchantResponse response = merchantService.reactivateMerchant(merchantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    //  API Key Management
    // ==========================================

    @PostMapping("/{merchantId}/api-keys")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createApiKey(
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreateApiKeyRequest request
    ) {
        ApiKeyResponse response = merchantService.createApiKey(merchantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{merchantId}/api-keys")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> listApiKeys(@PathVariable UUID merchantId) {
        List<ApiKeyResponse> keys = merchantService.listApiKeys(merchantId);
        return ResponseEntity.ok(ApiResponse.success(keys));
    }

    @DeleteMapping("/{merchantId}/api-keys/{keyId}")
    public ResponseEntity<ApiResponse<Void>> revokeApiKey(
            @PathVariable UUID merchantId,
            @PathVariable UUID keyId
    ) {
        merchantService.revokeApiKey(merchantId, keyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
