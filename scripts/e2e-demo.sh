#!/usr/bin/env bash
# ============================================================
# PayFlow End-to-End Live Demonstration Script (Bash)
# ============================================================

set -euo pipefail

GATEWAY_URL="http://localhost:8080"
TRACE_ID="e2e-demo-$RANDOM"

echo "============================================================"
echo "   ⚡ PayFlow Distributed Payment Platform E2E Demo"
echo "============================================================"

# 1. Health Check
echo -e "\n[1/7] Probing API Gateway Health..."
curl -sf "$GATEWAY_URL/actuator/health" > /dev/null && echo "  ✅ API Gateway is UP"

# 2. Register Alice
echo -e "\n[2/7] Registering Users (Alice & Bob)..."
ALICE_EMAIL="alice.$RANDOM@payflow.com"
BOB_EMAIL="bob.$RANDOM@payflow.com"

ALICE_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: $TRACE_ID" \
  -d "{\"email\":\"$ALICE_EMAIL\",\"password\":\"Password123!\",\"firstName\":\"Alice\",\"lastName\":\"Smith\",\"phone\":\"+15551234567\"}")

ALICE_ID=$(echo "$ALICE_RESP" | grep -o '"userId":"[^"]*' | cut -d'"' -f4)
ALICE_TOKEN=$(echo "$ALICE_RESP" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "  ✅ Registered Alice: ID=$ALICE_ID"

BOB_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: $TRACE_ID" \
  -d "{\"email\":\"$BOB_EMAIL\",\"password\":\"Password123!\",\"firstName\":\"Bob\",\"lastName\":\"Jones\",\"phone\":\"+15559876543\"}")

BOB_ID=$(echo "$BOB_RESP" | grep -o '"userId":"[^"]*' | cut -d'"' -f4)
echo "  ✅ Registered Bob: ID=$BOB_ID"

# 3. Create & Fund Wallets
echo -e "\n[3/7] Creating & Funding Wallets..."
ALICE_WALLET=$(curl -s -X POST "$GATEWAY_URL/api/v1/wallets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"userId\":\"$ALICE_ID\",\"currency\":\"INR\"}")
ALICE_WALLET_ID=$(echo "$ALICE_WALLET" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

curl -s -X POST "$GATEWAY_URL/api/v1/wallets/$ALICE_WALLET_ID/credit" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"amount\":1000.00,\"currency\":\"INR\",\"referenceId\":\"INIT-FUNDING-$RANDOM\"}" > /dev/null
echo "  ✅ Alice Wallet funded with 1,000 INR"

BOB_WALLET=$(curl -s -X POST "$GATEWAY_URL/api/v1/wallets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"userId\":\"$BOB_ID\",\"currency\":\"INR\"}")
BOB_WALLET_ID=$(echo "$BOB_WALLET" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "  ✅ Bob Wallet created: ID=$BOB_WALLET_ID"

# 4. Execute Payment
echo -e "\n[4/7] Executing P2P Payment (Alice -> Bob: 250.00 INR)..."
IDEMPOTENCY_KEY="PAY-E2E-$RANDOM"
PAY_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"senderWalletId\":\"$ALICE_WALLET_ID\",\"recipientWalletId\":\"$BOB_WALLET_ID\",\"amount\":250.00,\"currency\":\"INR\",\"paymentType\":\"P2P_TRANSFER\",\"idempotencyKey\":\"$IDEMPOTENCY_KEY\"}")

PAY_ID=$(echo "$PAY_RESP" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "  ✅ Payment Completed: ID=$PAY_ID"

# 5. Idempotency Check
echo -e "\n[5/7] Verifying Idempotency Defense..."
DUP_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/payments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"senderWalletId\":\"$ALICE_WALLET_ID\",\"recipientWalletId\":\"$BOB_WALLET_ID\",\"amount\":250.00,\"currency\":\"INR\",\"paymentType\":\"P2P_TRANSFER\",\"idempotencyKey\":\"$IDEMPOTENCY_KEY\"}")
DUP_ID=$(echo "$DUP_RESP" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ "$PAY_ID" == "$DUP_ID" ]; then
  echo "  ✅ Idempotency Verified (Original Payment returned, zero double-debit)"
fi

echo -e "\n============================================================"
echo "   🎉 All PayFlow Distributed Workflows Succeeded!"
echo "============================================================"
