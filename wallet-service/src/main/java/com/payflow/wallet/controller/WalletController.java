package com.payflow.wallet.controller;

import com.payflow.common.model.enums.WalletStatus;
import com.payflow.common.model.response.ApiResponse;
import com.payflow.common.security.UserPrincipal;
import com.payflow.wallet.dto.CreateWalletRequest;
import com.payflow.wallet.dto.WalletBalanceResponse;
import com.payflow.wallet.dto.WalletOperationRequest;
import com.payflow.wallet.dto.WalletResponse;
import com.payflow.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "Wallet creation, balance queries, top-up, withdrawal, and administration")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(summary = "Create a new wallet for the authenticated user")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) CreateWalletRequest request
    ) {
        WalletResponse response = walletService.createWallet(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{walletId}")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isOwner(authentication, #walletId)")
    @Operation(summary = "Get wallet details by ID (Owner or Admin only)")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(
            @PathVariable("walletId") UUID walletId
    ) {
        WalletResponse response = walletService.getWallet(walletId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isOwner(authentication, #walletId)")
    @Operation(summary = "Get authoritative wallet balance (Owner or Admin only)")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
            @PathVariable("walletId") UUID walletId
    ) {
        WalletBalanceResponse response = walletService.getBalance(walletId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{walletId}/top-up")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isOwner(authentication, #walletId)")
    @Operation(summary = "Top-up funds into wallet")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> topUp(
            @PathVariable("walletId") UUID walletId,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        WalletBalanceResponse response = walletService.topUp(walletId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{walletId}/withdraw")
    @PreAuthorize("hasRole('ADMIN') or @walletSecurity.isOwner(authentication, #walletId)")
    @Operation(summary = "Withdraw funds from wallet (Atomic conditional debit)")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> withdraw(
            @PathVariable("walletId") UUID walletId,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        WalletBalanceResponse response = walletService.withdraw(walletId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{walletId}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Freeze wallet (Admin only)")
    public ResponseEntity<ApiResponse<WalletResponse>> freezeWallet(
            @PathVariable("walletId") UUID walletId
    ) {
        WalletResponse response = walletService.updateWalletStatus(walletId, WalletStatus.FROZEN);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{walletId}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unfreeze wallet (Admin only)")
    public ResponseEntity<ApiResponse<WalletResponse>> unfreezeWallet(
            @PathVariable("walletId") UUID walletId
    ) {
        WalletResponse response = walletService.updateWalletStatus(walletId, WalletStatus.ACTIVE);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
