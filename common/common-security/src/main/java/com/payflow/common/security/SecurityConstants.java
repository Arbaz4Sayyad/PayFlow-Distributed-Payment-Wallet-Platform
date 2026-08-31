package com.payflow.common.security;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";

    public static final long ACCESS_TOKEN_VALIDITY_SECONDS = 900; // 15 minutes
    public static final long REFRESH_TOKEN_VALIDITY_SECONDS = 604800; // 7 days
}
