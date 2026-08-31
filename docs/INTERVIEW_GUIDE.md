# PayFlow — Staff/Senior Backend Engineer Interview Preparation Guide

This document contains deep-dive architectural questions, trade-off analyses, and model responses for Senior/Staff distributed systems and fintech engineering interviews.

---

## 1. Core System Design & Distributed Transactions

### Q1: Why choose Saga Orchestration over Choreography for a payment system?
> **Answer:**
> "In financial systems, **auditability, central state visibility, and strict compensation sequencing** are paramount:
> 1. **Centralized State Machine:** The orchestrator maintains an explicit finite state machine (`PaymentStatus`), allowing operators and monitoring systems to immediately query the exact stage of any inflight transaction without assembling events across 5 distributed logs.
> 2. **Controlled Backward Compensation:** In a transfer saga (Debit Sender → Credit Recipient → Ledger), if Credit Recipient fails, the orchestrator deterministically executes the exact compensating step (`creditSenderRefund`) and marks the saga `COMPENSATED`. In choreography, failure handling leads to cyclic event storms and cyclic dependency loops.
> 3. **Avoids Distributed Deadlocks:** The orchestrator enforces deterministic ordering (e.g., wallet IDs sorted lexicographically before acquiring locks), preventing circular lock wait chains."

---

### Q2: How do you guarantee zero duplicate debits when a client retries a payment request?
> **Answer:**
> "We implement a **two-tier idempotency defense**:
> 1. **Tier 1 (Fast-Path Distributed Lock in Redis):** Upon receiving `POST /api/v1/payments`, we acquire a Redis distributed lock on key `idempotency:lock:{senderWalletId}:{idempotencyKey}` with a 10-second TTL. If another thread is actively processing the same key, subsequent requests wait or fail immediately.
> 2. **Tier 2 (PostgreSQL Atomic Unique Constraint):** The `payments` table has a composite unique constraint `(sender_wallet_id, idempotency_key)`. If a concurrent request passes the Redis lock, the database insert throws a unique constraint violation (`409 Conflict`), triggering an immediate read of the original payment record and returning the cached response."

---

### Q3: Why use the Transactional Outbox pattern instead of publishing directly to Kafka inside `@Transactional`?
> **Answer:**
> "Directly publishing to Kafka inside a Spring `@Transactional` method causes the **Dual-Write Hazard**:
> - If Kafka publishing succeeds but the database transaction rolls back (e.g., due to an optimistic lock exception or constraint failure), downstream consumers process a 'ghost event' for a payment that does not exist in the DB.
> - If DB commits but the network to Kafka drops before publish, the event is permanently lost, causing ledger/wallet drift.
> 
> **With Transactional Outbox:**
> We insert the domain event into the `outbox_events` table in the **exact same ACID transaction** as the business entity update. An asynchronous Outbox Poller reads unpublished events and emits them to Kafka with `acks=all`. On consumer side, we pair this with an **Inbox Table (`processed_events`)** to achieve effective exactly-once semantics."

---

## 2. Concurrency & Financial Consistency

### Q4: Why use `long amountMinor` instead of `BigDecimal` or `float` for balances?
> **Answer:**
> "1. **Floating-point inaccuracies (`float`/`double`):** Binary IEEE 754 representations cannot accurately represent base-10 fractions (e.g., `0.1 + 0.2 = 0.30000000000000004`), leading to catastrophic compounding rounding errors in accounting balances.
> 2. **Primitive `long` performance:** Storing values in integer minor units (e.g., cents, paise: `$10.50 -> 1050L`) allows ultra-fast CPU atomic arithmetic, exact zero-remainder division checks, and compact database column storage (`BIGINT`).
> 3. **`BigDecimal` boundary:** `BigDecimal` is strictly used at the boundary/IO layers (REST DTOs) with explicit `RoundingMode.UNNECESSARY` validation before conversion to `long amountMinor`."

---

### Q5: How do you prevent race conditions when two concurrent transfers debit the same wallet?
> **Answer:**
> "We use **Atomic Conditional SQL Updates** with version checking:
> ```sql
> UPDATE wallets
> SET balance_minor = balance_minor - :amount,
>     version = version + 1,
>     updated_at = CURRENT_TIMESTAMP
> WHERE id = :walletId
>   AND balance_minor >= :amount;
> ```
> - If `rowsUpdated == 0`, we instantly detect whether the cause was insufficient funds or a concurrent write collision.
> - This avoids heavy pessimistic row locks (`SELECT FOR UPDATE`) which degrade throughput under high read load, while maintaining absolute balance non-negativity at the database engine level."

---

## 3. Resilience & Observability

### Q6: What happens if `ledger-service` goes down during a payment?
> **Answer:**
> "1. **Resilience4j Circuit Breaker:** The `payment-service` wraps `LedgerClient` calls in a circuit breaker. When failure rate exceeds 60%, the circuit opens, failing fast.
> 2. **Outbox Safety Net:** Because payment success is persisted and an outbox event `PaymentCompleted` is enqueued atomically, the payment itself is not rolled back.
> 3. **Eventual Consistency:** When `ledger-service` recovers, its idempotent Kafka consumer processes `payment.completed` from the topic and records the double-entry journal, guaranteeing eventual ledger reconciliation."

---

### Q7: How is distributed tracing propagated across async Kafka messages?
> **Answer:**
> "1. **HTTP Ingestion:** `TraceIdFilter` extracts `X-Trace-Id` or generates a 16-hex trace ID, placing it in SLF4J MDC.
> 2. **Outbox Serialization:** When `DomainEvent` is created, `metadata.traceId` captures the MDC trace ID.
> 3. **Kafka Headers:** The Outbox Poller sets Kafka header `X-Trace-Id: <traceId>`.
> 4. **Consumer Ingestion:** Kafka listener extracts header into MDC before executing channel send or ledger booking, providing seamless end-to-end trace correlation across all log aggregators (ELK/Datadog/CloudWatch)."
