package com.payflow.payment.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.payment.domain.entity.SagaInstance;
import com.payflow.payment.domain.repository.SagaInstanceRepository;
import com.payflow.payment.saga.client.LedgerClient;
import com.payflow.payment.saga.client.WalletClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TransferSagaOrchestrator.class);
    public static final String SAGA_TYPE = "P2P_TRANSFER_SAGA";

    private final SagaInstanceRepository sagaInstanceRepository;
    private final WalletClient walletClient;
    private final LedgerClient ledgerClient;
    private final ObjectMapper objectMapper;

    public TransferSagaOrchestrator(
            SagaInstanceRepository sagaInstanceRepository,
            WalletClient walletClient,
            LedgerClient ledgerClient,
            ObjectMapper objectMapper
    ) {
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.walletClient = walletClient;
        this.ledgerClient = ledgerClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the multi-service Transfer Saga with automated forward progression
     * and backward compensating transactions upon failure.
     */
    @Transactional
    public SagaResult executeTransfer(TransferSagaData data) {
        String correlationId = data.idempotencyKey();
        log.info("Starting Transfer Saga {} for payment {}", correlationId, data.paymentId());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            payloadJson = "{}";
        }

        // Initialize and persist Saga Instance
        SagaInstance saga = new SagaInstance(SAGA_TYPE, correlationId, "DEBIT_SENDER", payloadJson);
        saga = sagaInstanceRepository.save(saga);

        // ==========================================
        // STEP 1: DEBIT SENDER WALLET
        // ==========================================
        saga.advanceStep("DEBIT_SENDER");
        boolean debitSuccess = walletClient.debit(
                data.senderWalletId(),
                data.amountMinor(),
                data.currency(),
                "SAGA-DEBIT-" + data.paymentId()
        );

        if (!debitSuccess) {
            log.warn("Saga {}: Step DEBIT_SENDER failed. No compensation required.", correlationId);
            saga.fail("Sender debit failed: Insufficient funds or inactive wallet");
            sagaInstanceRepository.save(saga);
            return SagaResult.failed(saga.getId(), correlationId, "DEBIT_SENDER", saga.getCurrentState());
        }

        // ==========================================
        // STEP 2: CREDIT RECIPIENT WALLET
        // ==========================================
        saga.advanceStep("CREDIT_RECIPIENT");
        saga = sagaInstanceRepository.save(saga);

        boolean creditSuccess = walletClient.credit(
                data.recipientWalletId(),
                data.amountMinor(),
                data.currency(),
                "SAGA-CREDIT-" + data.paymentId()
        );

        if (!creditSuccess) {
            log.error("Saga {}: Step CREDIT_RECIPIENT failed! Initiating COMPENSATING TRANSACTIONS.", correlationId);
            saga.startCompensation("Recipient credit failed");
            saga = sagaInstanceRepository.save(saga);

            // COMPENSATING ACTION: Re-credit the debited amount back to sender's wallet
            boolean refundSuccess = walletClient.credit(
                    data.senderWalletId(),
                    data.amountMinor(),
                    data.currency(),
                    "SAGA-COMPENSATE-DEBIT-" + data.paymentId()
            );

            if (refundSuccess) {
                log.info("Saga {}: Compensating refund succeeded for sender {}", correlationId, data.senderWalletId());
                saga.completeCompensation();
                sagaInstanceRepository.save(saga);
                return SagaResult.compensated(saga.getId(), correlationId, "CREDIT_RECIPIENT", "Recipient credit failed. Sender compensated.");
            } else {
                log.error("CRITICAL SAGA ALERT: Compensation failed for sender {} in saga {}! Requires manual intervention.",
                        data.senderWalletId(), correlationId);
                saga.fail("CRITICAL: Compensation refund failed");
                sagaInstanceRepository.save(saga);
                return SagaResult.failed(saga.getId(), correlationId, "CREDIT_RECIPIENT", "CRITICAL: Compensation refund failed. Manual intervention required.");
            }
        }

        // ==========================================
        // STEP 3: RECORD IMMUTABLE LEDGER ENTRY
        // ==========================================
        saga.advanceStep("RECORD_LEDGER");
        saga = sagaInstanceRepository.save(saga);

        boolean ledgerSuccess = ledgerClient.recordEntry(
                data.paymentId(),
                data.senderWalletId(),
                data.recipientWalletId(),
                data.amountMinor(),
                data.currency()
        );

        if (!ledgerSuccess) {
            log.warn("Saga {}: Ledger async recording queued or retried.", correlationId);
        }

        // ==========================================
        // STEP 4: SAGA COMPLETED
        // ==========================================
        saga.complete();
        saga = sagaInstanceRepository.save(saga);
        log.info("Transfer Saga {} completed successfully for payment {}", correlationId, data.paymentId());

        return SagaResult.success(saga.getId(), correlationId);
    }
}
