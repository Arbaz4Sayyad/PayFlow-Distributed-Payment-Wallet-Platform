# PayFlow — Staff/Senior Backend Engineer Interview Preparation Guide

This document contains deep-dive architectural questions, system design trade-offs, concurrency models, failure modes, and battle-tested answers for Senior/Staff distributed systems and fintech engineering interviews at top product companies (e.g., Stripe, Razorpay, PhonePe, Uber, Amazon, Netflix, Square).

---

## 1. Core System Design & Distributed Transactions

### Q1: Why choose Saga Orchestration over Choreography for a payment system?
> **Answer:**
> "In financial systems, **auditability, central state visibility, and deterministic compensation sequencing** are critical:
> 1. **Centralized State Machine:** The orchestrator maintains an explicit finite state machine (`PaymentStatus`), allowing operators and monitoring systems to immediately query the exact stage of any inflight transaction without assembling events across 5 distributed logs.
> 2. **Controlled Backward Compensation:** In a transfer saga (`Debit Sender` → `Credit Recipient` → `Ledger`), if `Credit Recipient` fails, the orchestrator deterministically executes the exact compensating step (`creditSenderRefund`) and marks the saga `COMPENSATED`. In choreography, failure handling leads to cyclic event storms and cyclic dependency loops.
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
> We insert the domain event into the `outbox_events` table in the **exact same ACID transaction** as the business entity update. An asynchronous Outbox Poller reads unpublished events and emits them to Kafka with `acks=all`. On the consumer side, we pair this with an **Inbox Table (`processed_events`)** to achieve effective exactly-once semantics."

---

### Q4: How does PayFlow handle partial failures in a multi-step transfer saga?
> **Answer:**
> "When a transfer fails mid-execution:
> 1. **Debit Success + Credit Failure:** If recipient wallet cannot receive funds (e.g., wallet frozen or closed), the orchestrator triggers a compensating credit transaction on the sender's wallet with reference `REFUND-{paymentId}` and updates payment status to `FAILED_COMPENSATED`.
> 2. **Network Timeout during Credit:** If the HTTP call to credit the recipient times out, the orchestrator does **not** assume failure. It retries the call using the **same idempotency key** (`PAYFLOW-TXN-{paymentId}-CREDIT`). The recipient service recognizes the key and returns the existing result safely.
> 3. **Poison Pill / Unrecoverable Crash:** If compensation also fails (e.g., DB crash), the transaction is flagged in `dead_letter_sagas` for manual automated settlement replay via an asynchronous reconciliation worker."

---

## 2. Concurrency, Data Integrity & Double-Entry Accounting

### Q5: Why use `long amountMinor` instead of `BigDecimal` or `float` for balances?
> **Answer:**
> "1. **Floating-point inaccuracies (`float`/`double`):** Binary IEEE 754 representations cannot accurately represent base-10 fractions (e.g., `0.1 + 0.2 = 0.30000000000000004`), leading to catastrophic compounding rounding errors in accounting balances.
> 2. **Primitive `long` performance:** Storing values in integer minor units (e.g., cents, paise: `$10.50 -> 1050L`) allows ultra-fast CPU atomic arithmetic, exact zero-remainder division checks, and compact database column storage (`BIGINT`).
> 3. **`BigDecimal` boundary:** `BigDecimal` is strictly used at the boundary/IO layers (REST DTOs) with explicit `RoundingMode.UNNECESSARY` validation before conversion to `long amountMinor`."

---

### Q6: How do you prevent race conditions when two concurrent transfers debit the same wallet?
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

### Q7: What is Double-Entry Bookkeeping and how is it implemented in `ledger-service`?
> **Answer:**
> "In PayFlow, no balance ever changes in isolation. Every transaction is recorded as an immutable **Journal Entry** comprising balanced debits and credits across dual accounts:
> 
> $$\sum \text{Debits} = \sum \text{Credits}$$
> 
> **Ledger Entry Invariants:**
> 1. **Append-Only Immutability:** Ledger records are never updated or deleted (`UPDATE`/`DELETE` are revoked via PostgreSQL permissions). Corrections are booked as new reversing journal entries.
> 2. **Two-Legged Posting:**
>    - *P2P Transfer of ₹500:*
>      - `DEBIT` Sender Wallet Account (Asset) ₹500
>      - `CREDIT` Recipient Wallet Account (Liability/Asset) ₹500
>    - *Wallet Top-Up via Bank of ₹1,000:*
>      - `DEBIT` PayFlow Settlement Clearing Account ₹1,000
>      - `CREDIT` User Wallet Account ₹1,000
> 3. **Balance Reconciliation Invariant:** The wallet balance in `wallet-service` must always equal the sum of ledger postings in `ledger-service`:
>    $$\text{Wallet Balance} = \sum \text{Credits} - \sum \text{Debits}$$"

---

### Q8: What if a Redis distributed lock expires while the database transaction is still committing?
> **Answer:**
> "This is the classic **Distributed Lock Expiration Hazard**. We solve it using **Fencing Tokens & Database Optimistic Concurrency Control (OCC)**:
> 1. **Monotonic Fencing Token:** When acquiring a lock in Redis, we increment a monotonic version integer (`lock_version`).
> 2. **Database Verification:** When writing to PostgreSQL, the update queries include `WHERE version = :expectedVersion`.
> 3. **Lease Extension / Redisson Watchdog:** In Redis, a background watchdog thread periodically extends lock TTL while the transaction thread is actively executing.
> 4. **DB Constraint as Final Safety Net:** Even if lock safety fails completely, the composite database unique index `(sender_wallet_id, idempotency_key)` guarantees that duplicate commits are rejected by PostgreSQL."

---

## 3. Kafka, Messaging & Event-Driven Architecture

### Q9: How do you guarantee message order in Kafka across high-concurrency transfers?
> **Answer:**
> "In Kafka, message ordering is only guaranteed **within a single partition**.
> - **Partition Key Strategy:** We use the `senderWalletId` (or `walletId`) as the Kafka message key for all wallet mutation events (`wallet.debited`, `payment.initiated`).
> - **Consequence:** All sequential operations on Wallet A are routed to the exact same partition and consumed sequentially by the same consumer instance in strict FIFO order.
> - **Cross-Wallet Parallelism:** Transactions on Wallet B and Wallet C hash to different partitions, executing in parallel with maximum horizontal scalability."

---

### Q10: How do you handle poison pill messages and consumer deserialization crashes in Kafka?
> **Answer:**
> "We implement a **3-Tier Dead-Letter Queue (DLQ) & Retry Architecture**:
> 1. **Immediate In-Memory Retry (Exponential Backoff):** Non-blocking retry up to 3 times (e.g., 200ms, 800ms, 2000ms) for transient network timeouts.
> 2. **Retry Topic (`payment.events.retry`):** If in-memory retries are exhausted, the record is forwarded to a delayed retry topic with custom headers (`X-Retry-Count`, `X-Exception-Message`).
> 3. **Dead-Letter Topic (`payment.events.dlq`):** Permanent payload errors (schema corruption, missing mandatory fields) bypass retries and are sent directly to the DLQ.
> 4. **Automated Alerting:** DLQ lag triggers high-severity Prometheus alerts (`kafka_consumer_dlq_records_total > 0`), enabling on-call engineers to inspect and replay payloads via the admin workbench."

---

### Q11: How do you prevent duplicate message processing when a Kafka consumer group rebalances?
> **Answer:**
> "We implement the **Idempotent Consumer (Inbox) Pattern**:
> 1. Every Kafka event carries a unique `eventId` (UUIDv7) and `idempotencyKey`.
> 2. Before executing domain logic, the consumer executes:
>    ```sql
>    INSERT INTO processed_events (event_id, consumer_group, processed_at)
>    VALUES (:eventId, 'notification-group', CURRENT_TIMESTAMP)
>    ON CONFLICT (event_id, consumer_group) DO NOTHING;
>    ```
> 3. If `rowsInserted == 0`, the consumer knows this event was already processed before the rebalance, commits the Kafka offset immediately, and skips execution."

---

## 4. Security, Authentication & Zero-Trust Architecture

### Q12: How do you handle JWT revocation if tokens are stateless?
> **Answer:**
> "We use a **Hybrid Short-Lived JWT + Redis Revocation Bloom Filter / Blacklist**:
> 1. **Short Access Token Lifespan:** Access tokens are signed RS256 JWTs valid for only **15 minutes**.
> 2. **Redis Revocation Cache:** On user logout or security breach, the `jti` (JWT ID) is written to Redis key `auth:revoked:{jti}` with TTL equal to the remaining token lifetime.
> 3. **API Gateway Fast Check:** The Spring Cloud Gateway checks the Redis revocation set (cached in memory with sub-millisecond lookup) before forwarding the request.
> 4. **Refresh Token Rotation:** Long-lived refresh tokens (7 days) are stored in PostgreSQL with one-time use tokens. If a refresh token is reused, the entire token family is revoked immediately (detecting token theft)."

---

### Q13: How do you protect sensitive financial data (PII & Card/Account numbers)?
> **Answer:**
> "1. **Column-Level Encryption:** Sensitive fields (e.g., bank account numbers, tax IDs) are encrypted at rest using AES-256-GCM with keys managed by AWS KMS / HashiCorp Vault.
> 2. **Data Masking in Logs & DTOs:** Custom Jackson serializers mask account numbers (`•••• •••• 4832`) and emails (`j***e@domain.com`) across all log streams (Logback/MDC) and client-facing responses.
> 3. **PCI-DSS Scope Minimization:** No raw CVVs or plain card numbers ever touch PayFlow application memory or databases; payments use tokenized references."

---

### Q14: How does PayFlow prevent Distributed Denial of Service (DDoS) and API abuse?
> **Answer:**
> "We implement **Distributed Rate Limiting via Redis Sliding Window Counter**:
> - **Algorithm:** Uses Redis Sorted Sets (`ZADD`, `ZREMRANGEBYSCORE`, `ZCARD`) to evaluate precise request timestamps over rolling 60-second windows.
> - **Per-Tier Limits:**
>   - Public Endpoints (`/auth/login`): 10 req/min per IP.
>   - Authenticated Payments (`/api/v1/payments`): 100 req/min per User ID.
>   - Internal Webhooks: 1,000 req/min per Merchant Key.
> - When limit is breached, Gateway responds immediately with `HTTP 429 Too Many Requests` and standard `Retry-After: <seconds>` headers."

---

## 5. Scalability, Database Sharding & The Hot Wallet Problem

### Q15: How would you scale PayFlow from 1,000 TPS to 100,000 TPS?
> **Answer:**
> "1. **Database Sharding:** Shard the `wallets` and `ledger_entries` databases by `tenant_id` or `wallet_id % N` using Citus / Vitess, ensuring all writes for a specific wallet reside on a single shard node.
> 2. **CQRS (Command Query Responsibility Segregation):**
>    - *Write Path:* Scaled via partitioned Kafka ingestion and lightweight atomic row updates.
>    - *Read Path:* De-normalized query models streamed via Debezium CDC into Elasticsearch / Read-Replica PostgreSQL cluster for instantaneous history search.
> 3. **Redis Read-Through Caching:** Wallet balances and profile data are cached with TTL + Cache-Aside invalidation on update events."

---

### Q16: How do you solve the 'Hot Wallet' problem (e.g., Amazon or Uber receiving millions of credits/second)?
> **Answer:**
> "If millions of users pay a single merchant (e.g., Amazon), a single `wallets` row becomes a catastrophic write-lock bottleneck.
> 
> **Solution: Sharded / Partitioned Balances:**
> 1. Split the merchant's single logical wallet into $K$ sub-wallets (e.g., `merchant_wallet_sub_01` through `merchant_wallet_sub_16`).
> 2. Inbound credit transactions randomly select a sub-wallet ($i = \text{random}(1, K)$) and execute atomic credits in parallel across 16 database partitions.
> 3. When querying the total merchant balance, calculate:
>    $$\text{Total Balance} = \sum_{i=1}^{K} \text{sub\_wallet}_i.\text{balance}$$
> 4. For merchant payouts (debits), the payout worker aggregates funds across sub-wallets before disbursing."

---

## 6. Failure Modes, Chaos Engineering & Disaster Recovery

### Q17: What happens if `ledger-service` goes down during a payment?
> **Answer:**
> "1. **Resilience4j Circuit Breaker:** The `payment-service` wraps `LedgerClient` calls in a circuit breaker. When failure rate exceeds 60%, the circuit opens, failing fast.
> 2. **Outbox Safety Net:** Because payment success is persisted and an outbox event `PaymentCompleted` is enqueued atomically, the payment itself is not rolled back.
> 3. **Eventual Consistency:** When `ledger-service` recovers, its idempotent Kafka consumer processes `payment.completed` from the topic and records the double-entry journal, guaranteeing eventual ledger reconciliation."

---

### Q18: How is distributed tracing propagated across async Kafka messages?
> **Answer:**
> "1. **HTTP Ingestion:** `TraceIdFilter` extracts `X-Trace-Id` or generates a 16-hex trace ID, placing it in SLF4J MDC.
> 2. **Outbox Serialization:** When `DomainEvent` is created, `metadata.traceId` captures the MDC trace ID.
> 3. **Kafka Headers:** The Outbox Poller sets Kafka header `X-Trace-Id: <traceId>`.
> 4. **Consumer Ingestion:** Kafka listener extracts header into MDC before executing channel send or ledger booking, providing seamless end-to-end trace correlation across all log aggregators (ELK/Datadog/CloudWatch)."

---

### Q19: How do you detect and reconcile silent financial ledger discrepancies?
> **Answer:**
> "We implement an **Asynchronous Nightly Reconciliation Engine**:
> 1. **Batch Ledger Audit:** A background job runs at 00:00 UTC, calculating:
>    $$\Delta = \text{Wallet Balance} - (\sum \text{Ledger Credits} - \sum \text{Ledger Debits})$$
> 2. **Zero-Tolerance Assertion:** If $\Delta \neq 0$ for any wallet, the account is immediately flagged as `RECONCILIATION_DRIFT` and frozen for debit operations.
> 3. **Automated Drift Resolution:** The engine replays uncommitted outbox events or missing webhook logs from bank statements, generating an auditable `ADJUSTMENT` journal entry signed by system admin keys."

---

## 7. Behavioral & Production Incident Scenarios

### Q20: *'Tell me about the hardest distributed systems bug you encountered in PayFlow.'*
> **Answer Framework:**
> - **Situation:** Under heavy load testing (5,000 concurrent transfers), we noticed intermittent `OptimisticLockingFailureException` causing false-positive user payment failures.
> - **Root Cause:** Multiple concurrent debits against the same user caused database row version collisions; the client retried with a *new* idempotency key instead of reusing the original key, causing duplicate debit risks.
> - **Action Taken:** Replaced optimistic entity locks with **atomic conditional SQL decrement** (`WHERE balance_minor >= :amount`), introduced Redis-based client request de-bouncing at the API Gateway, and enforced exponential backoff jitter on internal saga retries.
> - **Result:** Transfer failure rate dropped from 4.2% to 0.001%, throughput increased by 3.8x, and idempotency guarantees remained 100% airtight.

---

### Q21: *'A payment of ₹50,000 was debited from Sender but never reached Recipient due to an unhandled exception. How do you respond?'*
> **Answer:**
> "1. **Triage & Containment:** Identify the transaction via `transactionNumber` / trace ID in ELK logs. Check the Saga state in `payment_db` (`status: IN_FLIGHT` or `FAILED`).
> 2. **Verify Ledger State:** Query `ledger_db` to confirm whether the sender debit journal entry exists and whether any corresponding credit was posted.
> 3. **Execute Deterministic Recovery:**
>    - If recipient wallet service is healthy, invoke the idempotent manual replay endpoint: `POST /api/v1/admin/sagas/{paymentId}/resume`.
>    - If recipient account is invalid, trigger the backward compensation workflow to refund ₹50,000 back to the sender's wallet with explicit ledger audit reference `MANUAL-COMPENSATION-{ticketId}`.
> 4. **Post-Mortem & Prevention:** Document Root Cause Analysis (RCA), add an integration test simulating this exact unhandled exception, and update the saga orchestrator timeout watchdog to auto-compensate transactions stalled > 5 minutes."

---

## Quick Reference Summary Table for Interviews

| Topic | Mechanism in PayFlow | Alternative Considered | Why PayFlow's Choice Wins |
|---|---|---|---|
| **Distributed Transactions** | Saga Orchestration | 2-Phase Commit (2PC) | 2PC locks resources across distributed coordinators; Saga provides high availability and asynchronous compensation. |
| **Consistency Guarantee** | Transactional Outbox + CDC | Direct Kafka Publish inside `@Transactional` | Prevents the Dual-Write Hazard; guarantees zero ghost events or lost notifications. |
| **Balance Representation** | Primitive `long amountMinor` | `float` / `double` / `BigDecimal` | `long` eliminates floating point rounding errors and executes in 1 CPU cycle. |
| **Concurrency Control** | Atomic SQL conditional update | Pessimistic `SELECT FOR UPDATE` | Prevents database connection pool exhaustion and deadlocks under concurrent read traffic. |
| **Duplicate Prevention** | Redis Lock + DB Unique Index | In-memory JVM lock | Multi-instance cluster safety; surviving node crashes and network retries. |
| **Message Ordering** | Kafka Partition Key (`walletId`) | Single Partition | Enables horizontal scaling across $N$ partitions while preserving strict per-wallet sequential order. |
