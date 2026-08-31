package com.payflow.fraud.rule;

public interface FraudRule {

    String getName();

    /**
     * Evaluates the transaction context and returns risk penalty points.
     * Points range: 0 (no risk) to 100 (instant block).
     */
    int evaluate(FraudEvaluationContext context);
}
