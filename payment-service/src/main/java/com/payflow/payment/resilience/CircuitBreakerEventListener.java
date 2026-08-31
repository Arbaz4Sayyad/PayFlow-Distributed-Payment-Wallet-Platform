package com.payflow.payment.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registers circuit breaker state-transition event listeners for observability.
 *
 * When a circuit breaker transitions CLOSED→OPEN, an alert-worthy event should
 * fire (PagerDuty, Alertmanager). This component logs transitions with WARN/ERROR
 * severity so they surface immediately in alerting pipelines.
 */
@Component
public class CircuitBreakerEventListener {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerEventListener.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerEventListener(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void registerEventListeners() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::registerListeners);

        // Also register for dynamically created CBs
        circuitBreakerRegistry.getEventPublisher()
                .onEntryAdded(event -> registerListeners(event.getAddedEntry()));
    }

    private void registerListeners(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> handleStateTransition(name, event))
                .onError(event -> log.warn("[CB:{}] Call failed: {} in {}ms",
                        name, event.getThrowable().getClass().getSimpleName(),
                        event.getElapsedDuration().toMillis()))
                .onSlowCallRateExceeded(event -> log.warn("[CB:{}] Slow call rate exceeded: {:.1f}%",
                        name, event.getSlowCallRate()))
                .onFailureRateExceeded(event -> log.error("[CB:{}] Failure rate exceeded threshold: {:.1f}%",
                        name, event.getFailureRate()));
    }

    private void handleStateTransition(String name, CircuitBreakerOnStateTransitionEvent event) {
        CircuitBreaker.State from = event.getStateTransition().getFromState();
        CircuitBreaker.State to = event.getStateTransition().getToState();

        if (to == CircuitBreaker.State.OPEN) {
            log.error("[CB:{}] CIRCUIT OPENED: {} → {}. Downstream service unavailable. Fallback active.",
                    name, from, to);
            // Production: publish alert to PagerDuty / Alertmanager here
        } else if (to == CircuitBreaker.State.HALF_OPEN) {
            log.warn("[CB:{}] Circuit HALF-OPEN: {} → {}. Probing downstream recovery.",
                    name, from, to);
        } else if (to == CircuitBreaker.State.CLOSED) {
            log.info("[CB:{}] Circuit CLOSED: {} → {}. Downstream service recovered.",
                    name, from, to);
        }
    }
}
