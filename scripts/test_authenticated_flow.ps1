$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmZDljOWY1NS05OTk1LTQzMDgtYjBkZC1lNDg1MTcyOTNiMTIiLCJ1c2VySWQiOiJmZDljOWY1NS05OTk1LTQzMDgtYjBkZC1lNDg1MTcyOTNiMTIiLCJlbWFpbCI6ImRldmVsb3BlckBwYXlmbG93LmNvbSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3ODgxNTczODUsImV4cCI6MTc4ODE1ODI4NX0.whu7uozNA98h9ViTHjfBr82wVkPiWfGkTqd6RtafWje0qrResM1F_xXo_GqjDZTlDQdrO0990DPERe2REayVQA"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 1. Create USD Wallet
$walletBody = @{
    currency = "USD"
} | ConvertTo-Json

try {
    $wallet = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wallets" -Method Post -Headers $headers -Body $walletBody
    Write-Host "Created Wallet:" ($wallet | ConvertTo-Json -Compress)
    $walletId = $wallet.data.id
} catch {
    $wallet = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wallets/me" -Method Get -Headers $headers
    Write-Host "Existing Wallet:" ($wallet | ConvertTo-Json -Compress)
    $walletId = $wallet.data.id
}

# 2. Top-up Wallet with $500.00 (50000 minor units)
$topupBody = @{
    amountMinor = 50000
    currency = "USD"
    referenceId = "TOPUP-" + [guid]::NewGuid().ToString()
} | ConvertTo-Json

$topupRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wallets/$walletId/top-up" -Method Post -Headers $headers -Body $topupBody
Write-Host "Top-Up Result:" ($topupRes | ConvertTo-Json -Compress)
