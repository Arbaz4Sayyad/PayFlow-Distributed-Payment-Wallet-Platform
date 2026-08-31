package com.payflow.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "mySuperSecretKeyForTestingPayFlowServiceLongEnoughForHmacSha256!";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(testSecret, 900, 604800);
    }

    @Test
    @DisplayName("Should generate, validate and extract claims from access token")
    void shouldGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "testuser@payflow.internal";
        Set<String> roles = Set.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtTokenProvider.generateAccessToken(userId, email, roles);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.extractEmail(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.extractRoles(token)).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should reject tampered or invalid token")
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }
}
