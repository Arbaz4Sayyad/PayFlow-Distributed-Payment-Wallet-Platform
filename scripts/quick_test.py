import urllib.request
import urllib.error
import ssl
import sys

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

services = [
    ("Frontend (Vercel)", "https://pay-flow-distributed-payment-wallet.vercel.app/"),
    ("API Gateway Root", "https://payflow-api-gateway-55ll.onrender.com/"),
    ("API Gateway Health", "https://payflow-api-gateway-55ll.onrender.com/actuator/health"),
    ("User Service Health", "https://payflow-user-service.onrender.com/actuator/health"),
    ("Wallet Service Health", "https://payflow-wallet-service-ued6.onrender.com/actuator/health"),
    ("Payment Service Health", "https://payflow-payment-service-j1a6.onrender.com/actuator/health"),
    ("Ledger Service Health", "https://payflow-ledger-service.onrender.com/actuator/health"),
    ("Merchant Service Health", "https://payflow-merchant-service-dv6u.onrender.com/actuator/health"),
    ("Fraud Service Health", "https://payflow-fraud-service-j4ew.onrender.com/actuator/health"),
    ("Notification Service Health", "https://payflow-notification-service-x4r4.onrender.com/actuator/health"),
]

for name, url in services:
    sys.stdout.write(f"\nChecking {name} ({url})...\n")
    sys.stdout.flush()
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=30, context=ctx) as resp:
            data = resp.read().decode('utf-8', errors='replace')
            sys.stdout.write(f"  [SUCCESS {resp.status}]: {data[:200]}\n")
            sys.stdout.flush()
    except urllib.error.HTTPError as e:
        err_data = e.read().decode('utf-8', errors='replace')
        sys.stdout.write(f"  [HTTP ERROR {e.code}]: {err_data[:200]}\n")
        sys.stdout.flush()
    except Exception as e:
        sys.stdout.write(f"  [FAILED]: {e}\n")
        sys.stdout.flush()
