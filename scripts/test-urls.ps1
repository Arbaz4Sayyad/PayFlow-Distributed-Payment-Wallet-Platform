$urls = @(
    "https://payflow-api-gateway-55ll.onrender.com",
    "https://payflow-api-gateway-55ll.onrender.com/actuator/health",
    "https://payflow-notification-service-x4r4.onrender.com",
    "https://payflow-notification-service-x4r4.onrender.com/actuator/health",
    "https://payflow-fraud-service-j4ew.onrender.com",
    "https://payflow-fraud-service-j4ew.onrender.com/actuator/health",
    "https://payflow-merchant-service-dv6u.onrender.com",
    "https://payflow-merchant-service-dv6u.onrender.com/actuator/health",
    "https://payflow-ledger-service.onrender.com",
    "https://payflow-ledger-service.onrender.com/actuator/health",
    "https://payflow-wallet-service-ued6.onrender.com",
    "https://payflow-wallet-service-ued6.onrender.com/actuator/health",
    "https://payflow-payment-service-j1a6.onrender.com",
    "https://payflow-payment-service-j1a6.onrender.com/actuator/health",
    "https://payflow-user-service.onrender.com",
    "https://payflow-user-service.onrender.com/actuator/health",
    "https://pay-flow-distributed-payment-wallet.vercel.app/",
    "https://pay-flow-distributed-payment-wallet.vercel.app/api/users/health"
)

foreach ($url in $urls) {
    Write-Host "========================================"
    Write-Host "Testing: $url"
    try {
        $resp = Invoke-WebRequest -Uri $url -Method Get -TimeoutSec 15 -UseBasicParsing
        Write-Host "Status: $($resp.StatusCode)"
        $len = [Math]::Min(300, $resp.Content.Length)
        Write-Host "Content: $($resp.Content.Substring(0, $len))"
    } catch {
        Write-Host "Error: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object System.IO.StreamReader($stream)
                $body = $reader.ReadToEnd()
                $len = [Math]::Min(300, $body.Length)
                Write-Host "Error Body: $($body.Substring(0, $len))"
            }
        }
    }
}
