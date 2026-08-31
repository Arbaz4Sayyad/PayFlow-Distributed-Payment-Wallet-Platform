package com.payflow.ledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.LedgerEntryType;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.ledger.dto.CreateJournalEntryRequest;
import com.payflow.ledger.dto.JournalEntryResponse;
import com.payflow.ledger.dto.JournalLineRequest;
import com.payflow.ledger.dto.JournalLineResponse;
import com.payflow.ledger.service.LedgerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LedgerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("LedgerController REST Endpoint Tests")
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LedgerService ledgerService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/ledger/entries should return 201 Created for balanced journal entry")
    void shouldRecordJournalEntry() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        UUID walletA = UUID.randomUUID();
        UUID walletB = UUID.randomUUID();

        CreateJournalEntryRequest request = new CreateJournalEntryRequest(
                txId,
                "Transfer",
                Currency.INR,
                List.of(
                        new JournalLineRequest(walletA, LedgerEntryType.DEBIT, new BigDecimal("200.00"), Currency.INR),
                        new JournalLineRequest(walletB, LedgerEntryType.CREDIT, new BigDecimal("200.00"), Currency.INR)
                )
        );

        JournalEntryResponse response = new JournalEntryResponse(
                entryId,
                txId,
                "Transfer",
                Currency.INR,
                Instant.now(),
                List.of(
                        new JournalLineResponse(UUID.randomUUID(), walletA, LedgerEntryType.DEBIT, new BigDecimal("200.00"), 20000L, "₹ 200.00", Instant.now()),
                        new JournalLineResponse(UUID.randomUUID(), walletB, LedgerEntryType.CREDIT, new BigDecimal("200.00"), 20000L, "₹ 200.00", Instant.now())
                ),
                true
        );

        when(ledgerService.recordJournalEntry(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/ledger/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value(txId.toString()))
                .andExpect(jsonPath("$.data.isBalanced").value(true))
                .andExpect(jsonPath("$.data.lines.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/ledger/entries should return 400 Bad Request when lines list has fewer than 2 lines")
    void shouldRejectSingleLineEntry() throws Exception {
        UUID txId = UUID.randomUUID();
        CreateJournalEntryRequest invalidRequest = new CreateJournalEntryRequest(
                txId,
                "Single leg entry (invalid)",
                Currency.INR,
                List.of(
                        new JournalLineRequest(UUID.randomUUID(), LedgerEntryType.DEBIT, new BigDecimal("100.00"), Currency.INR)
                )
        );

        mockMvc.perform(post("/api/v1/ledger/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
