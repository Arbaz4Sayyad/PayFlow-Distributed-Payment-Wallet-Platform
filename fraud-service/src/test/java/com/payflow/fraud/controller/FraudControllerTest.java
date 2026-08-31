package com.payflow.fraud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.fraud.domain.entity.BlacklistRecord;
import com.payflow.fraud.domain.enums.FraudDecision;
import com.payflow.fraud.domain.repository.FlaggedTransactionRepository;
import com.payflow.fraud.dto.AddBlacklistRequest;
import com.payflow.fraud.dto.FraudEvaluationRequest;
import com.payflow.fraud.dto.FraudEvaluationResponse;
import com.payflow.fraud.service.FraudEvaluationService;
import com.payflow.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FraudController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FraudController WebMvc Integration Tests")
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FraudEvaluationService fraudEvaluationService;

    @MockBean
    private FlaggedTransactionRepository flaggedTransactionRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/fraud/evaluate - Should evaluate transaction and return decision")
    void shouldEvaluateTransactionSuccessfully() throws Exception {
        UUID txId = UUID.randomUUID();
        FraudEvaluationRequest request = new FraudEvaluationRequest(
                txId, UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("100.00"), Currency.INR, "127.0.0.1", "DEV-1"
        );

        FraudEvaluationResponse response = FraudEvaluationResponse.of(
                txId, 10, FraudDecision.APPROVED, List.of(), "Low risk"
        );
        when(fraudEvaluationService.evaluate(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/fraud/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.decision").value("APPROVED"))
                .andExpect(jsonPath("$.data.riskScore").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/fraud/blacklist - Should add target to blacklist")
    void shouldAddToBlacklistSuccessfully() throws Exception {
        AddBlacklistRequest request = new AddBlacklistRequest("IP", "192.168.1.100", "Bot network");
        BlacklistRecord record = new BlacklistRecord("IP", "192.168.1.100", "Bot network");

        when(fraudEvaluationService.addToBlacklist(any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/fraud/blacklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetType").value("IP"))
                .andExpect(jsonPath("$.data.targetValue").value("192.168.1.100"));
    }
}
