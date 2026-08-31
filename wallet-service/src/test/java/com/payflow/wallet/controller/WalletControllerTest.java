package com.payflow.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.wallet.dto.WalletBalanceResponse;
import com.payflow.wallet.dto.WalletOperationRequest;
import com.payflow.wallet.security.WalletSecurity;
import com.payflow.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WalletController REST Endpoint Tests")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    @MockBean
    private WalletSecurity walletSecurity;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/v1/wallets/{walletId}/balance should return 200 OK with balance")
    void shouldReturnWalletBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletBalanceResponse balanceResponse = new WalletBalanceResponse(
                walletId,
                Currency.INR,
                new BigDecimal("1500.50"),
                150050L,
                "₹ 1500.50"
        );

        when(walletService.getBalance(walletId)).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/v1/wallets/{walletId}/balance", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.balanceMinor").value(150050))
                .andExpect(jsonPath("$.data.formattedBalance").value("₹ 1500.50"));
    }

    @Test
    @DisplayName("POST /api/v1/wallets/{walletId}/top-up should return 400 Bad Request for zero or negative amount")
    void shouldRejectInvalidTopUpAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletOperationRequest invalidRequest = new WalletOperationRequest(
                new BigDecimal("0.00"), // Invalid: must be >= 0.01
                Currency.INR,
                "REF-INVALID",
                "Zero amount"
        );

        mockMvc.perform(post("/api/v1/wallets/{walletId}/top-up", walletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
