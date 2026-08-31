package com.payflow.payment.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Resilience4j Circuit Breaker & Retry Unit Tests")
class CircuitBreakerRetryTest {

    private CircuitBreakerRegistry cbRegistry;
    private RetryRegistry retryRegistry;

    @BeforeEach
    void setUp() {
        // Use a small window for deterministic testing (no Spring context needed)
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .failureRateThreshold(60f)                 // Open after 60% failure
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        cbRegistry = CircuitBreakerRegistry.of(cbConfig);

        // Use RuntimeException as retry target since Supplier.get() cannot throw checked exceptions.
        // In production, RestTemplate / WebClient wrap IOException in unchecked ResourceAccessException.
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(RuntimeException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        retryRegistry = RetryRegistry.of(retryConfig);
    }

    @Test
    @DisplayName("Circuit breaker should OPEN after 60% failure rate in 5-call window")
    void shouldOpenCircuitAfterThresholdFailures() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("walletService");

        AtomicInteger callCount = new AtomicInteger(0);
        Supplier<Boolean> failingSupplier = CircuitBreaker.decorateSupplier(cb, () -> {
            callCount.incrementAndGet();
            throw new RuntimeException("wallet-service down");
        });

        // Drive 5 calls through — all fail (100% > 60% threshold)
        for (int i = 0; i < 5; i++) {
            try { failingSupplier.get(); } catch (Exception ignored) {}
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("Circuit breaker should CLOSED when failure rate stays below threshold")
    void shouldRemainClosedBelowThreshold() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("walletService");

        Supplier<Boolean> successSupplier = CircuitBreaker.decorateSupplier(cb, () -> true);

        // Drive 5 successful calls
        for (int i = 0; i < 5; i++) {
            successSupplier.get();
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("Retry should attempt 3 times before propagating RuntimeException")
    void shouldRetryExactlyThreeTimes() {
        Retry retry = retryRegistry.retry("walletService");

        AtomicInteger attemptCount = new AtomicInteger(0);
        // Supplier.get() cannot throw checked exceptions.
        // In production RestTemplate wraps IOException as unchecked ResourceAccessException.
        Supplier<Boolean> alwaysThrows = Retry.decorateSupplier(retry, () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("Connection refused"); // simulates ResourceAccessException
        });

        assertThatThrownBy(alwaysThrows::get)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Connection refused");

        assertThat(attemptCount.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Retry should NOT retry on ignored exception (IllegalArgumentException)")
    void shouldNotRetryOnIgnoredException() {
        Retry retry = retryRegistry.retry("walletService");

        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<Boolean> badRequest = Retry.decorateSupplier(retry, () -> {
            attemptCount.incrementAndGet();
            throw new IllegalArgumentException("Bad input");
        });

        assertThatThrownBy(badRequest::get)
                .isInstanceOf(IllegalArgumentException.class);

        // Only 1 attempt — no retry on ignored exception
        assertThat(attemptCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Retry should succeed on second attempt (transient failure recovery)")
    void shouldSucceedOnSecondAttempt() {
        Retry retry = retryRegistry.retry("walletService");

        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<Boolean> failOnceThenSucceed = Retry.decorateSupplier(retry, () -> {
            if (attemptCount.incrementAndGet() == 1) {
                throw new RuntimeException("Transient network hiccup"); // simulates ResourceAccessException
            }
            return true;
        });

        Boolean result = failOnceThenSucceed.get();
        assertThat(result).isTrue();
        assertThat(attemptCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Open circuit should reject calls immediately without calling downstream")
    void openCircuitShouldShortCircuit() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("walletService");

        AtomicInteger actualCallCount = new AtomicInteger(0);
        Supplier<Boolean> failingSupplier = CircuitBreaker.decorateSupplier(cb, () -> {
            actualCallCount.incrementAndGet();
            throw new RuntimeException("wallet-service down");
        });

        // Force circuit OPEN with 5 failures
        for (int i = 0; i < 5; i++) {
            try { failingSupplier.get(); } catch (Exception ignored) {}
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        int callsBeforeOpen = actualCallCount.get();

        // Next call should be short-circuited — downstream NOT invoked
        try { failingSupplier.get(); } catch (Exception ignored) {}
        assertThat(actualCallCount.get()).isEqualTo(callsBeforeOpen); // no new downstream call
    }

    @Test
    @DisplayName("Bulkhead config: failure rate threshold correctly set in registry")
    void circuitBreakerConfigShouldReflectThresholds() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("walletService");
        CircuitBreakerConfig config = cb.getCircuitBreakerConfig();

        assertThat(config.getFailureRateThreshold()).isEqualTo(60f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(5);
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(2);
    }
}
