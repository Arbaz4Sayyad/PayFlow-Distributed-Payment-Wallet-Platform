package com.payflow.payment.service;

import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.PaymentStatus;
import com.payflow.common.model.enums.PaymentType;
import com.payflow.common.model.exception.InvalidStateTransitionException;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.observability.PayFlowMetrics;
import com.payflow.payment.domain.entity.Payment;
import com.payflow.payment.domain.repository.PaymentRepository;
import com.payflow.payment.dto.InitiatePaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.gateway.GatewayRequest;
import com.payflow.payment.gateway.GatewayResponse;
import com.payflow.payment.gateway.PaymentGatewayProvider;
import com.payflow.payment.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService State Machine & Lifecycle Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayProvider gatewayProvider;

    @Mock
    private OutboxService outboxService;

    @Mock
    private PayFlowMetrics metrics;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, gatewayProvider, outboxService, metrics);
    }

    @Test
    @DisplayName("Should successfully execute payment: CREATED -> PROCESSING -> SUCCESS")
    void shouldCompletePaymentSuccessfully() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                sender,
                recipient,
                new BigDecimal("250.00"),
                Currency.INR,
                PaymentType.P2P_TRANSFER,
                "IDEMP-KEY-101",
                "Dinner split"
        );

        when(paymentRepository.findBySenderWalletIdAndIdempotencyKey(sender, "IDEMP-KEY-101"))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayProvider.execute(any(GatewayRequest.class)))
                .thenReturn(GatewayResponse.approved("PGW-SUCCESS-1"));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.amountMinor()).isEqualTo(25000L);
        assertThat(response.currency()).isEqualTo(Currency.INR);
    }

    @Test
    @DisplayName("Should mark payment FAILED when payment gateway declines")
    void shouldMarkPaymentFailedWhenGatewayDeclines() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                sender,
                recipient,
                new BigDecimal("500.00"),
                Currency.INR,
                PaymentType.MERCHANT_PAYMENT,
                "IDEMP-KEY-DECLINE",
                "Declined test"
        );

        when(paymentRepository.findBySenderWalletIdAndIdempotencyKey(sender, "IDEMP-KEY-DECLINE"))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayProvider.execute(any(GatewayRequest.class)))
                .thenReturn(GatewayResponse.declined("INSUFFICIENT_LIMIT", "Card limit exceeded"));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Card limit exceeded");
    }

    @Test
    @DisplayName("Should reject self-transfer when sender and recipient are identical")
    void shouldRejectSelfTransfer() {
        UUID sameWallet = UUID.randomUUID();

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                sameWallet,
                sameWallet, // Same
                new BigDecimal("100.00"),
                Currency.INR,
                PaymentType.P2P_TRANSFER,
                "IDEMP-KEY-SELF",
                "Self transfer"
        );

        when(paymentRepository.findBySenderWalletIdAndIdempotencyKey(sameWallet, "IDEMP-KEY-SELF"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment(request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("Sender and recipient wallets cannot be identical");

        verify(gatewayProvider, never()).execute(any());
    }

    @Test
    @DisplayName("Should idempotently return existing payment on duplicate request")
    void shouldReturnExistingPaymentOnDuplicateKey() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        Payment existing = new Payment(sender, recipient, 10000L, Currency.INR, PaymentType.P2P_TRANSFER, "DUP-KEY");
        existing.transitionTo(PaymentStatus.PROCESSING);
        existing.transitionTo(PaymentStatus.SUCCESS);

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                sender,
                recipient,
                new BigDecimal("100.00"),
                Currency.INR,
                PaymentType.P2P_TRANSFER,
                "DUP-KEY",
                "Replay"
        );

        when(paymentRepository.findBySenderWalletIdAndIdempotencyKey(sender, "DUP-KEY"))
                .thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.initiatePayment(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);
        verify(gatewayProvider, never()).execute(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully refund a SUCCESS payment")
    void shouldRefundSuccessfulPayment() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), 10000L, Currency.INR, PaymentType.MERCHANT_PAYMENT, "REF-KEY");
        payment.transitionTo(PaymentStatus.PROCESSING);
        payment.transitionTo(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.refundPayment(paymentId, "Customer return");

        assertThat(response.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(response.failureReason()).isEqualTo("Customer return");
    }

    @Test
    @DisplayName("Should reject illegal state transition: CREATED -> REFUNDED")
    void shouldRejectIllegalStateTransition() {
        Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), 10000L, Currency.INR, PaymentType.MERCHANT_PAYMENT, "TEST-KEY");
        // payment is currently CREATED

        assertThatThrownBy(() -> payment.transitionTo(PaymentStatus.REFUNDED))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Cannot transition PaymentStatus from CREATED to REFUNDED");
    }
}
