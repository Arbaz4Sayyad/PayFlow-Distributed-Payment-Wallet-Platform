$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmZDljOWY1NS05OTk1LTQzMDgtYjBkZC1lNDg1MTcyOTNiMTIiLCJ1c2VySWQiOiJmZDljOWY1NS05OTk1LTQzMDgtYjBkZC1lNDg1MTcyOTNiMTIiLCJlbWFpbCI6ImRldmVsb3BlckBwYXlmbG93LmNvbSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3ODgxNTczODUsImV4cCI6MTc4ODE1ODI4NX0.whu7uozNA98h9ViTHjfBr82wVkPiWfGkTqd6RtafWje0qrResM1F_xXo_GqjDZTlDQdrO0990DPERe2REayVQA"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 1. Get Wallet
$wallet = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wallets/1be4f2d0-31ea-4070-8dfd-d03af6f40c2e" -Method Get -Headers $headers
Write-Host "Wallet before:" ($wallet | ConvertTo-Json -Compress)

# 2. Top-Up $500.00
$topupBody = @{
    amount = 500.00
    currency = "USD"
    description = "Salary Deposit"
} | ConvertTo-Json

$res = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wallets/1be4f2d0-31ea-4070-8dfd-d03af6f40c2e/top-up" -Method Post -Headers $headers -Body $topupBody
Write-Host "Wallet after top-up:" ($res | ConvertTo-Json -Compress)
