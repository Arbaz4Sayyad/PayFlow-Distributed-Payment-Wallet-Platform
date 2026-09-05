import urllib.request
import urllib.error
import ssl
import time

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

urls = [
    ("Vercel App", "https://pay-flow-distributed-payment-wallet.vercel.app/"),
    ("API Gateway Root", "https://payflow-api-gateway-55ll.onrender.com/"),
    ("API Gateway Health", "https://payflow-api-gateway-55ll.onrender.com/actuator/health"),
    ("API Gateway Swagger", "https://payflow-api-gateway-55ll.onrender.com/swagger-ui.html"),
    ("User Service Health", "https://payflow-user-service.onrender.com/actuator/health"),
    ("Wallet Service Health", "https://payflow-wallet-service-ued6.onrender.com/actuator/health"),
    ("Payment Service Health", "https://payflow-payment-service-j1a6.onrender.com/actuator/health"),
    ("Ledger Service Health", "https://payflow-ledger-service.onrender.com/actuator/health"),
    ("Merchant Service Health", "https://payflow-merchant-service-dv6u.onrender.com/actuator/health"),
    ("Fraud Service Health", "https://payflow-fraud-service-j4ew.onrender.com/actuator/health"),
    ("Notification Service Health", "https://payflow-notification-service-x4r4.onrender.com/actuator/health"),
    ("Vercel -> Gateway API Proxy", "https://pay-flow-distributed-payment-wallet.vercel.app/api/v1/auth/health"),
    ("Gateway -> User Service Auth", "https://payflow-api-gateway-55ll.onrender.com/api/v1/auth/health")
]

for name, url in urls:
    print(f"\n--- Checking: {name} ({url}) ---")
    start = time.time()
    req = urllib.request.Request(
        url,
        headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}
    )
    try:
        with urllib.request.urlopen(req, timeout=60, context=ctx) as response:
            elapsed = time.time() - start
            body = response.read().decode('utf-8', errors='replace')
            print(f"Status: {response.status} in {elapsed:.2f}s")
            print(f"Response (first 300 chars): {body[:300]}")
    except urllib.error.HTTPError as e:
        elapsed = time.time() - start
        err_body = e.read().decode('utf-8', errors='replace')
        print(f"HTTPError {e.code}: {e.reason} in {elapsed:.2f}s")
        print(f"Error Body: {err_body[:300]}")
    except Exception as e:
        elapsed = time.time() - start
        print(f"Failed in {elapsed:.2f}s: {e}")
