package com.payflow.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtTokenProvider(
            @Value("${payflow.jwt.secret:payflowSuperSecretProductionKeyMustBeAtLeast256BitsLongForHmacSha256!}") String secret,
            @Value("${payflow.jwt.access-token-validity-seconds:900}") long accessTokenValiditySeconds,
            @Value("${payflow.jwt.refresh-token-validity-seconds:604800}") long refreshTokenValiditySeconds
    ) {
        String effectiveSecret = (secret != null && secret.trim().length() >= 32)
                ? secret.trim()
                : "payflowSuperSecretProductionKeyMustBeAtLeast256BitsLongForHmacSha256!";
        this.signingKey = Keys.hmacShaKeyFor(effectiveSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public String generateAccessToken(UUID userId, String email, Set<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenValiditySeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(SecurityConstants.CLAIM_USER_ID, userId.toString())
                .claim(SecurityConstants.CLAIM_EMAIL, email)
                .claim(SecurityConstants.CLAIM_ROLES, roles != null ? List.copyOf(roles) : List.of())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(SecurityConstants.CLAIM_USER_ID, userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        String userIdStr = extractClaims(token).get(SecurityConstants.CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    public String extractEmail(String token) {
        return extractClaims(token).get(SecurityConstants.CLAIM_EMAIL, String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> extractRoles(String token) {
        List<String> rolesList = extractClaims(token).get(SecurityConstants.CLAIM_ROLES, List.class);
        return rolesList != null ? new HashSet<>(rolesList) : Set.of();
    }
}
