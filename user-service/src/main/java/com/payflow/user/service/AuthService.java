package com.payflow.user.service;

import com.payflow.common.model.enums.UserRole;
import com.payflow.common.model.exception.PayFlowException;
import com.payflow.common.security.JwtTokenProvider;
import com.payflow.common.security.SecurityConstants;
import com.payflow.user.domain.entity.RefreshToken;
import com.payflow.user.domain.entity.User;
import com.payflow.user.domain.repository.RefreshTokenRepository;
import com.payflow.user.domain.repository.UserRepository;
import com.payflow.user.dto.AuthResponse;
import com.payflow.user.dto.LoginRequest;
import com.payflow.user.dto.RefreshTokenRequest;
import com.payflow.user.dto.RegisterRequest;
import com.payflow.user.dto.UserProfileResponse;
import com.payflow.user.util.TokenHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new PayFlowException("EMAIL_ALREADY_EXISTS", "A user with this email already exists", 409);
        }

        if (userRepository.existsByPhone(request.phone().trim())) {
            throw new PayFlowException("PHONE_ALREADY_EXISTS", "A user with this phone number already exists", 409);
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        UserRole role = request.role() != null ? request.role() : UserRole.ROLE_USER;

        User user = new User(
                request.email().toLowerCase().trim(),
                request.phone().trim(),
                hashedPassword,
                role,
                null
        );

        user = userRepository.save(user);
        log.info("User registered successfully. UserId: {}, Role: {}", user.getId(), user.getRole());

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new PayFlowException("INVALID_CREDENTIALS", "Invalid email or password", 401));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Failed login attempt for email: {}", email);
            throw new PayFlowException("INVALID_CREDENTIALS", "Invalid email or password", 401);
        }

        if (!user.isActive()) {
            throw new PayFlowException("ACCOUNT_INACTIVE", "Account is " + user.getStatus(), 403);
        }

        log.info("User logged in successfully. UserId: {}", user.getId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = TokenHashUtil.hashToken(request.refreshToken().trim());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashWithUser(tokenHash)
                .orElseThrow(() -> new PayFlowException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or does not exist", 401));

        User user = refreshToken.getUser();

        // Refresh Token Rotation (RTR) - Reuse Detection
        if (refreshToken.isRevoked()) {
            log.error("CRITICAL SECURITY ALERT: Revoked refresh token reuse detected for userId: {}. Revoking all sessions!", user.getId());
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new PayFlowException(
                    "TOKEN_REUSE_DETECTED",
                    "Suspicious activity: Refresh token reuse detected. All active sessions have been terminated.",
                    401
            );
        }

        if (refreshToken.isExpired()) {
            throw new PayFlowException("REFRESH_TOKEN_EXPIRED", "Refresh token has expired. Please log in again.", 401);
        }

        if (!user.isActive()) {
            throw new PayFlowException("ACCOUNT_INACTIVE", "User account is no longer active", 403);
        }

        // Revoke the old token (one-time use)
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // Issue brand new token pair
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = TokenHashUtil.hashToken(rawRefreshToken.trim());
            refreshTokenRepository.findByTokenHashWithUser(tokenHash).ifPresent(token -> {
                token.revoke();
                refreshTokenRepository.save(token);
                log.info("Refresh token revoked for userId: {}", token.getUser().getId());
            });
        }
    }

    private AuthResponse issueTokens(User user) {
        Set<String> roles = Set.of(user.getRole().name());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String rawRefreshToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        String tokenHash = TokenHashUtil.hashToken(rawRefreshToken);
        Instant expiry = Instant.now().plus(SecurityConstants.REFRESH_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiry);
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                rawRefreshToken,
                SecurityConstants.ACCESS_TOKEN_VALIDITY_SECONDS,
                UserProfileResponse.fromEntity(user)
        );
    }
}
