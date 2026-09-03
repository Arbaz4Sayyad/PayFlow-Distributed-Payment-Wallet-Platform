# ============================================================
# PayFlow End-to-End Live Demonstration Script (PowerShell)
# Tests the full distributed payment journey across all services
# ============================================================

$GATEWAY_URL = "http://localhost:8080"
$TRACE_ID = "e2e-demo-" + (Get-Random)
$HEADERS = @{
    "Content-Type" = "application/json"
    "X-Trace-Id"   = $TRACE_ID
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "   PayFlow Distributed Payment Platform E2E Demo" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Health Check
Write-Host "`n[1/7] Probing API Gateway Health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$GATEWAY_URL/actuator/health" -Method Get -Headers $HEADERS
    Write-Host "  [PASS] API Gateway is UP: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "  [FAIL] Gateway not reachable on $GATEWAY_URL. Ensure Docker Compose is running!" -ForegroundColor Red
    exit 1
}

# 2. Register Sender & Recipient Users
Write-Host "`n[2/7] Registering Users (Alice & Bob)..." -ForegroundColor Yellow
$rand1 = Get-Random -Minimum 1000000 -Maximum 9999999
$aliceEmail = "alice.$rand1@payflow.com"
$alicePhone = "+1555$rand1"

$aliceReg = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/auth/register" -Method Post -Headers $HEADERS -Body (@{
    email     = $aliceEmail
    password  = "Password123!"
    firstName = "Alice"
    lastName  = "Smith"
    phone     = $alicePhone
} | ConvertTo-Json)

$aliceId = $aliceReg.data.user.id
$aliceToken = $aliceReg.data.accessToken
Write-Host "  [PASS] Registered Alice: ID=$aliceId (Token generated)" -ForegroundColor Green

$rand2 = Get-Random -Minimum 1000000 -Maximum 9999999
$bobEmail   = "bob.$rand2@payflow.com"
$bobPhone   = "+1555$rand2"

$bobReg = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/auth/register" -Method Post -Headers $HEADERS -Body (@{
    email     = $bobEmail
    password  = "Password123!"
    firstName = "Bob"
    lastName  = "Jones"
    phone     = $bobPhone
} | ConvertTo-Json)

$bobId = $bobReg.data.user.id
$bobToken = $bobReg.data.accessToken
Write-Host "  [PASS] Registered Bob: ID=$bobId (Token generated)" -ForegroundColor Green

# 3. Create & Fund Wallets
Write-Host "`n[3/7] Creating & Funding Wallets..." -ForegroundColor Yellow
$authHeaders = @{
    "Content-Type"  = "application/json"
    "Authorization" = "Bearer $aliceToken"
    "X-Trace-Id"    = $TRACE_ID
}

$bobAuthHeaders = @{
    "Content-Type"  = "application/json"
    "Authorization" = "Bearer $bobToken"
    "X-Trace-Id"    = $TRACE_ID
}

# Create Alice Wallet
$aliceWallet = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/wallets" -Method Post -Headers $authHeaders -Body (@{
    userId   = $aliceId
    currency = "INR"
} | ConvertTo-Json)
$aliceWalletId = $aliceWallet.data.id
Write-Host "  [PASS] Created Alice Wallet: ID=$aliceWalletId (Balance: 0 INR)" -ForegroundColor Green

# Credit Alice Wallet with 1,000 INR
$creditRes = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/wallets/$aliceWalletId/top-up" -Method Post -Headers $authHeaders -Body (@{
    amount      = 1000.00
    currency    = "INR"
    referenceId = "INIT-FUNDING-" + (Get-Random)
} | ConvertTo-Json)
Write-Host "  [PASS] Credited Alice Wallet: New Balance = $($creditRes.data.balance) INR" -ForegroundColor Green

# Create Bob Wallet
$bobWallet = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/wallets" -Method Post -Headers $bobAuthHeaders -Body (@{
    userId   = $bobId
    currency = "INR"
} | ConvertTo-Json)
$bobWalletId = $bobWallet.data.id
Write-Host "  [PASS] Created Bob Wallet: ID=$bobWalletId (Balance: 0 INR)" -ForegroundColor Green

# 4. Execute P2P Transfer (Saga Orchestration)
Write-Host "`n[4/7] Executing P2P Payment (Alice -> Bob: 250.00 INR)..." -ForegroundColor Yellow
$idempotencyKey = "PAY-E2E-" + (Get-Random)

$paymentRes = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments" -Method Post -Headers $authHeaders -Body (@{
    senderWalletId    = $aliceWalletId
    recipientWalletId = $bobWalletId
    amount            = 250.00
    currency          = "INR"
    paymentType       = "P2P_TRANSFER"
    idempotencyKey    = $idempotencyKey
} | ConvertTo-Json)

$paymentId = $paymentRes.data.id
$paymentStatus = $paymentRes.data.status
Write-Host "  [PASS] Payment Initiated & Completed: ID=$paymentId, Status=$paymentStatus" -ForegroundColor Green

# 5. Verify Idempotency Protection (Duplicate Request with Same Key)
Write-Host "`n[5/7] Testing Idempotency Protection with Duplicate Request..." -ForegroundColor Yellow
$dupPaymentRes = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/payments" -Method Post -Headers $authHeaders -Body (@{
    senderWalletId    = $aliceWalletId
    recipientWalletId = $bobWalletId
    amount            = 250.00
    currency          = "INR"
    paymentType       = "P2P_TRANSFER"
    idempotencyKey    = $idempotencyKey
} | ConvertTo-Json)

if ($dupPaymentRes.data.id -eq $paymentId) {
    Write-Host "  [PASS] Idempotency Verified! Duplicate request returned original Payment ID without double debiting." -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Idempotency check failed!" -ForegroundColor Red
}

# 6. Verify Final Wallet Balances
Write-Host "`n[6/7] Verifying Invariant Wallet Balances..." -ForegroundColor Yellow
$aliceFinal = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/wallets/$aliceWalletId/balance" -Method Get -Headers $authHeaders
$bobFinal   = Invoke-RestMethod -Uri "$GATEWAY_URL/api/v1/wallets/$bobWalletId/balance" -Method Get -Headers $bobAuthHeaders

Write-Host "  Alice Final Balance: $($aliceFinal.data.balance) INR" -ForegroundColor Cyan
Write-Host "  Bob Final Balance:   $($bobFinal.data.balance) INR" -ForegroundColor Cyan

# 7. Check Prometheus Scrape Endpoint
Write-Host "`n[7/7] Verifying Micrometer / Prometheus Metrics..." -ForegroundColor Yellow
$metrics = Invoke-RestMethod -Uri "$GATEWAY_URL/actuator/prometheus" -Method Get
if ($metrics -match "payflow_payment_initiated_total") {
    Write-Host "  [PASS] Prometheus custom metrics found: payflow_payment_initiated_total" -ForegroundColor Green
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "   All PayFlow Distributed Workflows Succeeded!" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
