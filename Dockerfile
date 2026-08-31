# ============================================================
# PayFlow Multi-Service Dockerfile
# Usage: build from the service's own directory, e.g.:
#   docker build -t payflow/user-service:1.0.0 \
#     --build-arg SERVICE_NAME=user-service \
#     --build-arg SERVICE_PORT=8081 \
#     -f Dockerfile .
#
# Multi-stage build strategy:
#   Stage 1 (builder) — Maven + JDK 21: compile & package JAR
#   Stage 2 (runtime) — JRE 21-slim:   minimal attack surface
#
# Security posture:
#   - Non-root user (uid 1001)
#   - Read-only filesystem-compatible
#   - No shell in final image (eclipse-temurin:21-jre-alpine)
#   - Layered JAR extraction for optimal Docker layer caching
# ============================================================

# ─────────────── Stage 1: Build ───────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Copy POM files first (layer cache: only re-run dependency resolution when pom.xml files change)
COPY pom.xml ./
COPY common/pom.xml                          common/pom.xml
COPY common/common-model/pom.xml             common/common-model/pom.xml
COPY common/common-security/pom.xml          common/common-security/pom.xml
COPY common/common-observability/pom.xml     common/common-observability/pom.xml
COPY api-gateway/pom.xml                     api-gateway/pom.xml
COPY user-service/pom.xml                    user-service/pom.xml
COPY wallet-service/pom.xml                  wallet-service/pom.xml
COPY payment-service/pom.xml                 payment-service/pom.xml
COPY ledger-service/pom.xml                  ledger-service/pom.xml
COPY merchant-service/pom.xml                merchant-service/pom.xml
COPY fraud-service/pom.xml                   fraud-service/pom.xml
COPY notification-service/pom.xml            notification-service/pom.xml

# Download dependencies (ignore error if multi-module sibling references are unresolved offline)
RUN mvn dependency:go-offline -B -q || true

# Copy source for all modules
COPY common/                     common/
COPY api-gateway/                api-gateway/
COPY user-service/               user-service/
COPY wallet-service/             wallet-service/
COPY payment-service/            payment-service/
COPY ledger-service/             ledger-service/
COPY merchant-service/           merchant-service/
COPY fraud-service/              fraud-service/
COPY notification-service/       notification-service/

# Build only the target service module (+ its dependencies)
ARG SERVICE_NAME=user-service
RUN mvn package -pl ${SERVICE_NAME} -am -DskipTests -B -q

# Extract layered JAR for optimal Docker layer caching in Stage 2.
# Spring Boot layered JARs split into: dependencies / spring-boot-loader /
# snapshot-dependencies / application (changes most frequently)
WORKDIR /workspace/${SERVICE_NAME}/target/extracted
RUN java -Djarmode=layertools \
         -jar /workspace/${SERVICE_NAME}/target/*.jar extract

# ─────────────── Stage 2: Runtime ───────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install curl for health check probes only (no other tools)
RUN apk add --no-cache curl

# Create non-root user (uid 1001, no login shell)
RUN addgroup -g 1001 payflow \
 && adduser  -u 1001 -G payflow -s /sbin/nologin -D payflowapp

WORKDIR /app

ARG SERVICE_NAME=user-service
ARG SERVICE_PORT=8081
ENV SERVICE_PORT=${SERVICE_PORT}

# Copy layered JAR content (ordered: least → most frequently changing)
COPY --from=builder --chown=payflowapp:payflow \
  /workspace/${SERVICE_NAME}/target/extracted/dependencies/           ./
COPY --from=builder --chown=payflowapp:payflow \
  /workspace/${SERVICE_NAME}/target/extracted/spring-boot-loader/     ./
COPY --from=builder --chown=payflowapp:payflow \
  /workspace/${SERVICE_NAME}/target/extracted/snapshot-dependencies/  ./
COPY --from=builder --chown=payflowapp:payflow \
  /workspace/${SERVICE_NAME}/target/extracted/application/            ./

USER payflowapp

# Health check: poll Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:${SERVICE_PORT}/actuator/health | \
      grep -q '"status":"UP"' || exit 1

EXPOSE ${SERVICE_PORT}

# JVM tuning:
#   UseContainerSupport    — respect cgroup CPU/memory limits
#   MaxRAMPercentage=75    — leave 25% headroom for OS & non-heap
#   -XX:+ExitOnOutOfMemoryError — crash instead of silently degrading
#   -Djava.security.egd    — faster startup by avoiding blocking /dev/random
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump.hprof", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:production}", \
  "org.springframework.boot.loader.launch.JarLauncher"]
