package com.payflow.ledger.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.ledger.dto.AuditBalanceReport;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalEntryResponse;
import com.payflow.ledger.dto.JournalLineResponse;
import com.payflow.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Ledger", description = "Immutable double-entry journal, transaction queries, and ledger integrity audits")
@SecurityRequirement(name = "bearerAuth")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/entries")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record an immutable, balanced double-entry journal entry")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> recordJournalEntry(
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        JournalEntryResponse response = ledgerService.recordJournalEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get journal entry and all line postings by transaction ID")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getJournalByTransactionId(
            @PathVariable("transactionId") UUID transactionId
    ) {
        JournalEntryResponse response = ledgerService.getJournalByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/wallets/{walletId}")
    @Operation(summary = "Get paginated double-entry statement for a specific wallet")
    public ResponseEntity<ApiResponse<Page<JournalLineResponse>>> getWalletStatement(
            @PathVariable("walletId") UUID walletId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<JournalLineResponse> statement = ledgerService.getWalletStatement(walletId, pageable);
        return ResponseEntity.ok(ApiResponse.success(statement));
    }

    @GetMapping("/audit/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Execute full double-entry accounting integrity check: Sum(Debits) == Sum(Credits)")
    public ResponseEntity<ApiResponse<AuditBalanceReport>> verifyLedgerIntegrity() {
        AuditBalanceReport report = ledgerService.verifyLedgerIntegrity();
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
