package com.payflow.merchant.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiKeyUtil Unit Tests")
class ApiKeyUtilTest {

    @Test
    @DisplayName("Generated key should start with pf_live_ prefix")
    void shouldGenerateKeyWithCorrectPrefix() {
        String rawKey = ApiKeyUtil.generateRawKey();
        assertThat(rawKey).startsWith("pf_live_");
        assertThat(rawKey.length()).isGreaterThan(16);
    }

    @Test
    @DisplayName("Hashing same raw key twice should produce identical SHA-256 hash")
    void hashShouldBeDeterministic() {
        String rawKey = ApiKeyUtil.generateRawKey();
        String hash1 = ApiKeyUtil.hashKey(rawKey);
        String hash2 = ApiKeyUtil.hashKey(rawKey);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Different raw keys should produce different hashes (collision resistance)")
    void differentKeysShouldProduceDifferentHashes() {
        String key1 = ApiKeyUtil.generateRawKey();
        String key2 = ApiKeyUtil.generateRawKey();
        assertThat(ApiKeyUtil.hashKey(key1)).isNotEqualTo(ApiKeyUtil.hashKey(key2));
    }

    @Test
    @DisplayName("SHA-256 hash should be 64 hex characters long")
    void hashShouldHaveCorrectLength() {
        String rawKey = ApiKeyUtil.generateRawKey();
        String hash = ApiKeyUtil.hashKey(rawKey);
        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("Two separately generated keys should never be equal (uniqueness)")
    void generatedKeysShouldBeUnique() {
        String key1 = ApiKeyUtil.generateRawKey();
        String key2 = ApiKeyUtil.generateRawKey();
        assertThat(key1).isNotEqualTo(key2);
    }
}
