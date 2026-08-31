package com.payflow.fraud.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.fraud.domain.entity.BlacklistRecord;
import com.payflow.fraud.domain.entity.FlaggedTransaction;
import com.payflow.fraud.domain.repository.FlaggedTransactionRepository;
import com.payflow.fraud.dto.AddBlacklistRequest;
import com.payflow.fraud.dto.FraudEvaluationRequest;
import com.payflow.fraud.dto.FraudEvaluationResponse;
import com.payflow.fraud.service.FraudEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fraud")
public class FraudController {

    private final FraudEvaluationService fraudEvaluationService;
    private final FlaggedTransactionRepository flaggedTransactionRepository;

    public FraudController(
            FraudEvaluationService fraudEvaluationService,
            FlaggedTransactionRepository flaggedTransactionRepository
    ) {
        this.fraudEvaluationService = fraudEvaluationService;
        this.flaggedTransactionRepository = flaggedTransactionRepository;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<FraudEvaluationResponse>> evaluate(
            @Valid @RequestBody FraudEvaluationRequest request
    ) {
        FraudEvaluationResponse response = fraudEvaluationService.evaluate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/blacklist")
    public ResponseEntity<ApiResponse<BlacklistRecord>> addToBlacklist(
            @Valid @RequestBody AddBlacklistRequest request
    ) {
        BlacklistRecord record = fraudEvaluationService.addToBlacklist(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(record));
    }

    @GetMapping("/flagged/{userId}")
    public ResponseEntity<ApiResponse<List<FlaggedTransaction>>> getFlaggedTransactions(
            @PathVariable UUID userId
    ) {
        List<FlaggedTransaction> transactions = flaggedTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
