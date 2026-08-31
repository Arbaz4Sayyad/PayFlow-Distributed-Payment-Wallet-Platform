package com.payflow.merchant.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utility for generating and hashing API keys.
 *
 * Security model:
 * - Raw key is generated from 32 cryptographically random bytes (CSPRNG).
 * - Key is prefixed with "pf_live_" for quick identification in logs/files.
 * - Only SHA-256(raw_key) is persisted — never the raw key itself.
 * - Even if the database is compromised, the attacker cannot derive raw keys from hashes.
 */
public final class ApiKeyUtil {

    public static final String KEY_PREFIX = "pf_live_";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private ApiKeyUtil() {
    }

    /**
     * Generates a cryptographically secure raw API key.
     * Format: pf_live_<base64url-encoded-32-bytes>
     */
    public static String generateRawKey() {
        byte[] rawBytes = new byte[32];
        SECURE_RANDOM.nextBytes(rawBytes);
        return KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(rawBytes);
    }

    /**
     * Hashes a raw API key using SHA-256 for secure storage.
     * Deterministic: same input always produces same output.
     */
    public static String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available on this JVM", e);
        }
    }
}
