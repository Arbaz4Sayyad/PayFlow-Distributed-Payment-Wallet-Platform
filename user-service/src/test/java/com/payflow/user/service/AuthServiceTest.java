package com.payflow.user.service;

import com.payflow.common.model.enums.UserRole;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.user.domain.entity.RefreshToken;
import com.payflow.user.domain.entity.User;
import com.payflow.user.domain.repository.RefreshTokenRepository;
import com.payflow.user.domain.repository.UserRepository;
import com.payflow.user.dto.AuthResponse;
import com.payflow.user.dto.LoginRequest;
import com.payflow.user.dto.RefreshTokenRequest;
import com.payflow.user.dto.RegisterRequest;
import com.payflow.user.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests (BCrypt, JWT, Refresh Token Rotation)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    @DisplayName("Should successfully register a new user and return tokens")
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "john.doe@payflow.internal",
                "+919876543210",
                "StrongPassword123!",
                UserRole.ROLE_USER
        );

        when(userRepository.existsByEmail("john.doe@payflow.internal")).thenReturn(false);
        when(userRepository.existsByPhone("+919876543210")).thenReturn(false);
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("$2a$12$hashedPasswordExample");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(), eq("john.doe@payflow.internal"), any()))
                .thenReturn("mock.access.token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock.access.token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("john.doe@payflow.internal");

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "existing@payflow.internal",
                "+919876543210",
                "Password123!",
                UserRole.ROLE_USER
        );

        when(userRepository.existsByEmail("existing@payflow.internal")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("A user with this email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("user@payflow.internal", "CorrectPassword123!");
        User user = new User("user@payflow.internal", "+919876543210", "$2a$12$hash", UserRole.ROLE_USER, null);

        when(userRepository.findByEmail("user@payflow.internal")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectPassword123!", "$2a$12$hash")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), eq("user@payflow.internal"), any()))
                .thenReturn("new.access.token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("new.access.token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest("user@payflow.internal", "WrongPassword!");
        User user = new User("user@payflow.internal", "+919876543210", "$2a$12$hash", UserRole.ROLE_USER, null);

        when(userRepository.findByEmail("user@payflow.internal")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword!", "$2a$12$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("Should rotate refresh token and revoke the previous one")
    void shouldRotateRefreshTokenSuccessfully() {
        String rawToken = "sampleValidRefreshTokenString12345";
        String tokenHash = TokenHashUtil.hashToken(rawToken);

        User user = new User("user@payflow.internal", "+919876543210", "hash", UserRole.ROLE_USER, null);
        RefreshToken oldToken = new RefreshToken(user, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));

        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(oldToken));
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), any())).thenReturn("rotated.access.token");

        AuthResponse response = authService.refreshToken(new RefreshTokenRequest(rawToken));

        assertThat(response.accessToken()).isEqualTo("rotated.access.token");
        assertThat(oldToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(oldToken);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should detect revoked token reuse and invalidate all user sessions")
    void shouldDetectTokenReuseAndRevokeAllSessions() {
        String rawToken = "alreadyUsedRefreshToken";
        String tokenHash = TokenHashUtil.hashToken(rawToken);

        User user = new User("user@payflow.internal", "+919876543210", "hash", UserRole.ROLE_USER, null);
        RefreshToken alreadyRevokedToken = new RefreshToken(user, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS));
        alreadyRevokedToken.revoke(); // Already used

        when(refreshTokenRepository.findByTokenHashWithUser(tokenHash)).thenReturn(Optional.of(alreadyRevokedToken));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(rawToken)))
                .isInstanceOf(PayFlowException.class)
                .hasMessageContaining("Refresh token reuse detected");

        // Assert that all active sessions for this user were invalidated
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }
}
