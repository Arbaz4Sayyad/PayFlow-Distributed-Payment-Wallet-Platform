package com.payflow.payment.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.SagaStatus;
import com.payflow.payment.domain.entity.SagaInstance;
import com.payflow.payment.domain.repository.SagaInstanceRepository;
import com.payflow.payment.saga.client.LedgerClient;
import com.payflow.payment.saga.client.WalletClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferSagaOrchestrator Distributed Saga Tests")
class TransferSagaOrchestratorTest {

    @Mock
    private SagaInstanceRepository sagaInstanceRepository;

    @Mock
    private WalletClient walletClient;

    @Mock
    private LedgerClient ledgerClient;

    private TransferSagaOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new TransferSagaOrchestrator(
                sagaInstanceRepository,
                walletClient,
                ledgerClient,
                new ObjectMapper()
        );
        when(sagaInstanceRepository.save(any(SagaInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Happy Path: Debit -> Credit -> Ledger -> Saga COMPLETED")
    void shouldCompleteSagaSuccessfully() {
        UUID paymentId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        TransferSagaData data = new TransferSagaData(paymentId, sender, recipient, 10000L, Currency.INR, "SAGA-KEY-1");

        when(walletClient.debit(eq(sender), eq(10000L), eq(Currency.INR), contains("SAGA-DEBIT"))).thenReturn(true);
        when(walletClient.credit(eq(recipient), eq(10000L), eq(Currency.INR), contains("SAGA-CREDIT"))).thenReturn(true);
        when(ledgerClient.recordEntry(eq(paymentId), eq(sender), eq(recipient), eq(10000L), eq(Currency.INR))).thenReturn(true);

        SagaResult result = orchestrator.executeTransfer(data);

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.status()).isEqualTo(SagaStatus.COMPLETED);

        verify(walletClient).debit(eq(sender), eq(10000L), eq(Currency.INR), any());
        verify(walletClient).credit(eq(recipient), eq(10000L), eq(Currency.INR), any());
        verify(ledgerClient).recordEntry(eq(paymentId), eq(sender), eq(recipient), eq(10000L), eq(Currency.INR));
    }

    @Test
    @DisplayName("Step 1 Failure: Sender debit fails -> Saga FAILED (No compensation needed)")
    void shouldFailSagaWhenDebitFails() {
        UUID paymentId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        TransferSagaData data = new TransferSagaData(paymentId, sender, recipient, 50000L, Currency.INR, "SAGA-KEY-2");

        when(walletClient.debit(eq(sender), eq(50000L), eq(Currency.INR), any())).thenReturn(false);

        SagaResult result = orchestrator.executeTransfer(data);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.status()).isEqualTo(SagaStatus.FAILED);
        assertThat(result.finalStep()).isEqualTo("DEBIT_SENDER");

        // Recipient credit and compensation MUST NOT be called
        verify(walletClient, never()).credit(any(), anyLong(), any(), any());
        verify(ledgerClient, never()).recordEntry(any(), any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Step 2 Failure: Recipient credit fails -> Compensate sender -> Saga COMPENSATED")
    void shouldCompensateSenderWhenRecipientCreditFails() {
        UUID paymentId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        TransferSagaData data = new TransferSagaData(paymentId, sender, recipient, 20000L, Currency.INR, "SAGA-KEY-3");

        // Step 1: Debit succeeds
        when(walletClient.debit(eq(sender), eq(20000L), eq(Currency.INR), any())).thenReturn(true);
        // Step 2: Recipient credit fails (e.g. frozen wallet)
        when(walletClient.credit(eq(recipient), eq(20000L), eq(Currency.INR), contains("SAGA-CREDIT"))).thenReturn(false);
        // Compensation: Sender refund succeeds
        when(walletClient.credit(eq(sender), eq(20000L), eq(Currency.INR), contains("SAGA-COMPENSATE-DEBIT"))).thenReturn(true);

        SagaResult result = orchestrator.executeTransfer(data);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.status()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(result.finalStep()).isEqualTo("CREDIT_RECIPIENT");

        // Verify forward debit and compensating credit occurred
        verify(walletClient).debit(eq(sender), eq(20000L), eq(Currency.INR), any());
        verify(walletClient).credit(eq(recipient), eq(20000L), eq(Currency.INR), any());
        verify(walletClient).credit(eq(sender), eq(20000L), eq(Currency.INR), contains("SAGA-COMPENSATE"));
        // Ledger was not recorded
        verify(ledgerClient, never()).recordEntry(any(), any(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("Step 2 Critical Failure: Recipient credit fails AND compensation refund fails")
    void shouldHandleCriticalCompensationFailure() {
        UUID paymentId = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();

        TransferSagaData data = new TransferSagaData(paymentId, sender, recipient, 20000L, Currency.INR, "SAGA-KEY-4");

        when(walletClient.debit(eq(sender), anyLong(), any(), any())).thenReturn(true);
        when(walletClient.credit(eq(recipient), anyLong(), any(), contains("SAGA-CREDIT"))).thenReturn(false);
        // Compensation fails!
        when(walletClient.credit(eq(sender), anyLong(), any(), contains("SAGA-COMPENSATE"))).thenReturn(false);

        SagaResult result = orchestrator.executeTransfer(data);

        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.status()).isEqualTo(SagaStatus.FAILED);
    }
}
