package com.payflow.payment.controller;

import com.payflow.common.model.response.ApiResponse;
import com.payflow.payment.dto.InitiatePaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.dto.RefundPaymentRequest;
import com.payflow.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment initiation, status tracking, and refunds")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Initiate a payment transaction")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey
    ) {
        // If header provided, override body to ensure uniform consistency
        InitiatePaymentRequest effectiveRequest = headerIdempotencyKey != null && !headerIdempotencyKey.isBlank()
                ? new InitiatePaymentRequest(
                        request.senderWalletId(),
                        request.recipientWalletId(),
                        request.amount(),
                        request.currency(),
                        request.paymentType(),
                        headerIdempotencyKey,
                        request.description()
                )
                : request;

        PaymentResponse response = paymentService.initiatePayment(effectiveRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment transaction details by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable("paymentId") UUID paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a previously completed payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable("paymentId") UUID paymentId,
            @Valid @RequestBody RefundPaymentRequest request
    ) {
        PaymentResponse response = paymentService.refundPayment(paymentId, request.reason());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
