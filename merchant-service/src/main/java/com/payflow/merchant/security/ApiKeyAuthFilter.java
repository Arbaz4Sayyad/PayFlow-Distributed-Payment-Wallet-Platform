package com.payflow.merchant.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that extracts and validates API keys from the
 * Authorization header: "Bearer pf_live_<key>"
 *
 * Runs once per request before the standard authentication chain.
 * On success, populates the SecurityContext with a merchant principal.
 * On failure, logs the attempt and passes through to a 401 response.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiKeyAuthenticator apiKeyAuthenticator;

    public ApiKeyAuthFilter(ApiKeyAuthenticator apiKeyAuthenticator) {
        this.apiKeyAuthenticator = apiKeyAuthenticator;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String rawKey = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            UserDetails merchantPrincipal = apiKeyAuthenticator.authenticate(rawKey);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            merchantPrincipal, null, merchantPrincipal.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BadCredentialsException ex) {
            log.warn("API key authentication rejected for path {}: {}", request.getRequestURI(), ex.getMessage());
            // Do NOT set SecurityContext — downstream will return 401
        }

        filterChain.doFilter(request, response);
    }
}
