package com.payflow.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that establishes a trace correlation context for every HTTP request.
 *
 * Behaviour:
 * - Reads X-Trace-Id from incoming request header (propagated from API gateway or client).
 * - If absent, generates a new 16-char hex trace ID (UUID without dashes, first 16 chars).
 * - Injects traceId into MDC so every log line in this request's thread automatically
 *   includes [traceId=abc123] — enabling log aggregation queries by trace ID.
 * - Reflects the traceId in X-Trace-Id response header for client-side correlation.
 * - Clears ALL MDC keys in a finally block — no MDC leakage across thread pool reuse.
 *
 * Used by all PayFlow microservices by including common-observability as a dependency.
 */
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = request.getHeader(MdcConstants.TRACE_HEADER);

        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        MDC.put(MdcConstants.TRACE_ID, traceId);
        // Span ID: unique per service hop (useful for distributed tracing fan-out)
        MDC.put(MdcConstants.SPAN_ID, UUID.randomUUID().toString().replace("-", "").substring(0, 8));

        response.setHeader(MdcConstants.TRACE_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Critical: clear ALL MDC keys to prevent thread-pool context leakage
            MDC.remove(MdcConstants.TRACE_ID);
            MDC.remove(MdcConstants.SPAN_ID);
            MDC.remove(MdcConstants.USER_ID);
            MDC.remove(MdcConstants.TRANSACTION_ID);
        }
    }
}
