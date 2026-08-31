$authUrl = "http://localhost:8080/api/v1/auth/login"
$loginBody = @{
    email = "developer@payflow.com"
    password = "Password123!"
} | ConvertTo-Json

try {
    $loginRes = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json"
} catch {
    $registerUrl = "http://localhost:8080/api/v1/auth/register"
    $registerBody = @{
        email = "developer@payflow.com"
        phone = "+1234567890"
        password = "Password123!"
    } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri $registerUrl -Method Post -Body $registerBody -ContentType "application/json"
}

$token = $loginRes.data.accessToken
$userId = $loginRes.data.user.id
Write-Host "USER_ID: $userId"
Write-Host "TOKEN: $token"
