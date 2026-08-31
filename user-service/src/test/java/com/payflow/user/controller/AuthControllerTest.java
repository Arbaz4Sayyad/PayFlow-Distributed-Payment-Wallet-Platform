package com.payflow.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.model.enums.UserRole;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.user.dto.AuthResponse;
import com.payflow.user.dto.RegisterRequest;
import com.payflow.user.dto.UserProfileResponse;
import com.payflow.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController REST Endpoint Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created with tokens for valid request")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "alice@payflow.internal",
                "+919876543210",
                "StrongPassword123!",
                UserRole.ROLE_USER
        );

        UserProfileResponse profile = new UserProfileResponse(
                UUID.randomUUID(),
                "alice@payflow.internal",
                "+919876543210",
                "ACTIVE",
                UserRole.ROLE_USER,
                null,
                Instant.now()
        );

        AuthResponse authResponse = AuthResponse.of("mock.jwt.token", "mock.refresh.token", 900, profile);
        when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.user.email").value("alice@payflow.internal"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 400 Bad Request when email is invalid")
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "invalid-email-format",
                "+919876543210",
                "Short",
                UserRole.ROLE_USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.validationErrors").isArray());
    }
}
