package com.payflow.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.currency.Currency;
import com.payflow.common.model.enums.PaymentStatus;
import com.payflow.common.model.enums.PaymentType;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.payment.dto.InitiatePaymentRequest;
import com.payflow.payment.dto.PaymentResponse;
import com.payflow.payment.service.PaymentService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PaymentController REST Endpoint Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/payments should return 201 Created for valid payment")
    void shouldInitiatePaymentSuccessfully() throws Exception {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                sender,
                recipient,
                new BigDecimal("150.00"),
                Currency.INR,
                PaymentType.P2P_TRANSFER,
                "IDEMP-KEY-REST",
                "Coffee"
        );

        PaymentResponse response = new PaymentResponse(
                paymentId,
                sender,
                recipient,
                new BigDecimal("150.00"),
                15000L,
                Currency.INR,
                PaymentStatus.SUCCESS,
                PaymentType.P2P_TRANSFER,
                "IDEMP-KEY-REST",
                null,
                Instant.now(),
                Instant.now()
        );

        when(paymentService.initiatePayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "IDEMP-KEY-REST")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.amountMinor").value(15000));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{paymentId} should return 200 OK")
    void shouldGetPaymentById() throws Exception {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                paymentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("200.00"),
                20000L,
                Currency.INR,
                PaymentStatus.PROCESSING,
                PaymentType.MERCHANT_PAYMENT,
                "IDEMP-GET",
                null,
                Instant.now(),
                Instant.now()
        );

        when(paymentService.getPayment(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }
}
