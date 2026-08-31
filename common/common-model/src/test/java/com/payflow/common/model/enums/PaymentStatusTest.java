package com.payflow.common.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentStatus FSM Transition Tests")
class PaymentStatusTest {

    @Test
    @DisplayName("Valid transitions should be permitted")
    void validTransitionsShouldSucceed() {
        assertThat(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.PROCESSING)).isTrue();
        assertThat(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.FAILED)).isTrue();
        assertThat(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.SUCCESS)).isTrue();
        assertThat(PaymentStatus.PROCESSING.canTransitionTo(PaymentStatus.FAILED)).isTrue();
        assertThat(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.REFUND_PENDING)).isTrue();
        assertThat(PaymentStatus.REFUND_PENDING.canTransitionTo(PaymentStatus.REFUNDED)).isTrue();
    }

    @Test
    @DisplayName("Invalid jumps must be strictly rejected")
    void invalidJumpsShouldFail() {
        assertThat(PaymentStatus.SUCCESS.canTransitionTo(PaymentStatus.PROCESSING)).isFalse();
        assertThat(PaymentStatus.FAILED.canTransitionTo(PaymentStatus.SUCCESS)).isFalse();
        assertThat(PaymentStatus.REFUNDED.canTransitionTo(PaymentStatus.CREATED)).isFalse();
        assertThat(PaymentStatus.CREATED.canTransitionTo(PaymentStatus.REFUNDED)).isFalse();
        assertThat(PaymentStatus.PROCESSING.canTransitionTo(null)).isFalse();
    }

    @Test
    @DisplayName("Terminal states should not allow any next transition")
    void terminalStatesShouldHaveNoExits() {
        assertThat(PaymentStatus.FAILED.isTerminal()).isTrue();
        assertThat(PaymentStatus.REFUNDED.isTerminal()).isTrue();
        assertThat(PaymentStatus.CANCELLED.isTerminal()).isTrue();
        assertThat(PaymentStatus.SUCCESS.isTerminal()).isFalse();
        assertThat(PaymentStatus.PROCESSING.isTerminal()).isFalse();
    }
}
