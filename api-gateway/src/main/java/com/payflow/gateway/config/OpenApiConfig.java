package com.payflow.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "⚡ PayFlow — Distributed Fintech & Digital Wallet Engine", version = "v1.0.0", description = """
                # 🏛️ PayFlow Distributed Architecture & API Portal

                **PayFlow** is a production-grade distributed payment and digital wallet platform built with **Java 21, Spring Boot 3, Apache Kafka, PostgreSQL, Redis, and Kubernetes**.

                ---
                ### 🚀 Quick Links & Interactive Resources
                * 🐙 **[GitHub Repository & Source Code](https://github.com/Arbaz4Sayyad/PayFlow-Distributed-Payment-Wallet-Platform)** — Architecture diagrams, test suites, and Docker deployment.
                * 📦 **[Postman Public Collection](https://www.postman.com/)** — Complete automated test suite with dynamic Bearer Token propagation.
                * 📊 **[Live Grafana Dashboards](http://localhost:3000)** — Real-time metrics, p99 latency, and Saga compensation monitors.
                * 📈 **[Prometheus Time-Series Metrics](http://localhost:9090)** — System & business metrics scraper.

                ---
                ### 💳 Core Fintech Architecture & Invariants
                1. **Minor-Unit Accounting (`amountMinor`):** Eliminates floating-point rounding errors (e.g., `$10.50` -> `1050L`).
                2. **Immutable Double-Entry Ledger:** Mathematical balance constraint `SUM(debits) == SUM(credits)` with zero ledger drift.
                3. **Saga Orchestrator with Compensating Transactions:** Backward compensation (`creditSenderRefund`) on credit leg failures.
                4. **Two-Tier Idempotency Engine:** Fast-path Redis distributed locking + database unique constraints on `(sender_wallet_id, idempotency_key)`.
                5. **Outbox & Inbox Pattern:** Transactional database polling to eliminate dual-write hazards between PostgreSQL and Kafka.

                ---
                ### 🔑 How to Test Live in this Swagger UI
                1. Select **`1. User & Auth Service`** from the **Select a definition** dropdown in the top right.
                2. Execute **`POST /api/v1/auth/register`** to create a user and copy the returned `accessToken`.
                3. Click the green **Authorize 🔓** button at the top, paste the token, and test any microservice!
                """, contact = @Contact(name = "PayFlow Engineering Team", email = "engineering@payflow.com"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer", description = "Enter your JWT Bearer token obtained from POST /api/v1/auth/login or /api/v1/auth/register")
public class OpenApiConfig {
}
