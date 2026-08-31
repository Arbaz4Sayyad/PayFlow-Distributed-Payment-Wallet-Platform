# PayFlow — Production Operations & On-Call Runbook

This runbook outlines operational triage steps, escalation paths, and recovery procedures for common production incidents.

---

## 1. Alert Triage Guide

### 🚨 Alert 1: `PayFlowSagaCompensationFailed`
- **PromQL Trigger:** `sum(rate(payflow_saga_compensation_failed_total[1m])) > 0`
- **Severity:** P0 (Critical - Financial Data Inconsistency)
- **Description:** A payment failed on a forward step (e.g., Credit Recipient), and the subsequent backward compensation (Refund Sender) **also failed**. Sender funds are in limbo.
- **Triage Steps:**
  1. Open Grafana and search logs for: `CRITICAL SAGA ALERT: Compensation failed`.
  2. Extract `paymentId`, `senderWalletId`, and `amountMinor`.
  3. Query database:
     ```sql
     SELECT * FROM payments WHERE id = '<paymentId>';
     SELECT * FROM wallet_journals WHERE reference_id = '<paymentId>';
     ```
  4. If sender was debited but not refunded, invoke the administrative refund tool:
     ```bash
     curl -X POST http://api-gateway:8080/api/v1/payments/<paymentId>/refund \
       -H "Authorization: Bearer $ADMIN_TOKEN" \
       -d '{"reason": "Manual on-call compensation recovery"}'
     ```

---

### 🚨 Alert 2: `HighCircuitBreakerOpenRate`
- **PromQL Trigger:** `resilience4j_circuitbreaker_state{state="open"} == 1`
- **Severity:** P1 (High Availability Degradation)
- **Description:** Circuit breaker opened for `walletService` or `ledgerService`. Downstream service is unreachable or throwing errors.
- **Triage Steps:**
  1. Check target pod health in Kubernetes:
     ```bash
     kubectl get pods -n payflow -l app=wallet-service
     kubectl logs -n payflow -l app=wallet-service --tail=100
     ```
  2. Inspect database connection pool exhaustion in HikariCP:
     ```bash
     kubectl exec -it -n payflow deploy/postgres -- psql -U postgres -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';"
     ```
  3. If pods are crash-looping due to OOM, scale up pod memory limits in `values.yaml`.

---

### 🚨 Alert 3: `KafkaConsumerLagHigh`
- **PromQL Trigger:** `kafka_consumergroup_lag{topic="payment.completed"} > 1000`
- **Severity:** P2 (Moderate Eventual Consistency Delay)
- **Description:** `notification-service` or `ledger-service` is processing messages slower than the incoming payment rate.
- **Triage Steps:**
  1. Check if consumers are undergoing repeated rebalances due to slow processing:
     ```bash
     kubectl logs -n payflow -l app=notification-service | grep "Rebalance"
     ```
  2. Scale consumer replicas to match Kafka partition count:
     ```bash
     kubectl scale deployment/notification-service -n payflow --replicas=6
     ```

---

## 2. Emergency Operational Commands

### 2.1 Drain and Restart a Pod Gracefully
```bash
kubectl rollout restart deployment/payment-service -n payflow
kubectl rollout status deployment/payment-service -n payflow
```

### 2.2 Instant Rollback of Last Deployment
```bash
helm rollback payflow -n payflow
```

### 2.3 Flush Redis Idempotency Cache (During Major Lock Deadlock)
```bash
kubectl exec -it -n payflow deploy/redis-service -- redis-cli -a "$REDIS_PASSWORD" EVAL "return redis.call('del', unpack(redis.call('keys', 'idempotency:lock:*')))" 0
```
