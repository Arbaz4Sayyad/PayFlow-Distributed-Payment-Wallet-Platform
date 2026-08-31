# PayFlow — REST API Reference & OpenAPI Specification

All endpoints are accessible via the **API Gateway** on port `8080` (or in production at `https://api.payflow.com`).

---

## 1. Authentication & User Service (`/api/v1/auth`, `/api/v1/users`)

### 1.1 Register User
- **Method / Path:** `POST /api/v1/auth/register`
- **Auth Required:** No
- **Request Body:**
  ```json
  {
    "email": "alice@payflow.com",
    "password": "Password123!",
    "firstName": "Alice",
    "lastName": "Smith",
    "phone": "+15551234567"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "success": true,
    "data": {
      "userId": "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "rt_live_9f8e2cdaf442ca63866d7f6e1fdac",
      "expiresIn": 900
    },
    "traceId": "4f8e2cdaf442ca63"
  }
  ```

### 1.2 Login & Token Rotation
- **Method / Path:** `POST /api/v1/auth/login`
- **Request Body:**
  ```json
  {
    "email": "alice@payflow.com",
    "password": "Password123!"
  }
  ```
- **Response (200 OK):** Same payload as registration.

### 1.3 Refresh Access Token
- **Method / Path:** `POST /api/v1/auth/refresh`
- **Request Body:**
  ```json
  {
    "refreshToken": "rt_live_9f8e2cdaf442ca63866d7f6e1fdac"
  }
  ```
- **Response (200 OK):** New access token and rotated refresh token.

---

## 2. Digital Wallet Service (`/api/v1/wallets`)

### 2.1 Create Wallet
- **Method / Path:** `POST /api/v1/wallets`
- **Headers:** `Authorization: Bearer <JWT>`
- **Request Body:**
  ```json
  {
    "userId": "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
    "currency": "INR"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "success": true,
    "data": {
      "id": "11111111-1111-1111-1111-111111111111",
      "userId": "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      "balanceMinor": 0,
      "currency": "INR",
      "status": "ACTIVE",
      "version": 0
    }
  }
  ```

### 2.2 Credit Wallet
- **Method / Path:** `POST /api/v1/wallets/{walletId}/credit`
- **Request Body:**
  ```json
  {
    "amount": 1000.00,
    "currency": "INR",
    "referenceId": "TOPUP-20260831-001"
  }
  ```
- **Response (200 OK):** Updated wallet entity with new `balanceMinor` (`100000L`).

### 2.3 Debit Wallet (Atomic Conditional)
- **Method / Path:** `POST /api/v1/wallets/{walletId}/debit`
- **Request Body:**
  ```json
  {
    "amount": 250.00,
    "currency": "INR",
    "referenceId": "DEBIT-20260831-001"
  }
  ```
- **Response (200 OK):** Updated wallet entity with new `balanceMinor` (`75000L`).
- **Error (422 Unprocessable Entity):** `INSUFFICIENT_FUNDS` if balance < requested amount.

---

## 3. Payment Service & Saga Orchestrator (`/api/v1/payments`)

### 3.1 Initiate Payment / P2P Transfer (Idempotent)
- **Method / Path:** `POST /api/v1/payments`
- **Headers:** `Authorization: Bearer <JWT>`, `X-Trace-Id: <uuid>`
- **Request Body:**
  ```json
  {
    "senderWalletId": "11111111-1111-1111-1111-111111111111",
    "recipientWalletId": "22222222-2222-2222-2222-222222222222",
    "amount": 250.00,
    "currency": "INR",
    "paymentType": "P2P_TRANSFER",
    "idempotencyKey": "PAY-TX-987654321"
  }
  ```
- **Response (201 Created / 200 OK on Idempotent Replay):**
  ```json
  {
    "success": true,
    "data": {
      "id": "98765432-1234-5678-9abc-def012345678",
      "senderWalletId": "11111111-1111-1111-1111-111111111111",
      "recipientWalletId": "22222222-2222-2222-2222-222222222222",
      "amountMinor": 25000,
      "currency": "INR",
      "status": "SUCCESS",
      "idempotencyKey": "PAY-TX-987654321",
      "createdAt": "2026-08-31T01:05:00Z"
    },
    "traceId": "4f8e2cdaf442ca63"
  }
  ```

### 3.2 Refund Payment
- **Method / Path:** `POST /api/v1/payments/{paymentId}/refund`
- **Request Body:**
  ```json
  {
    "reason": "Customer return"
  }
  ```
- **Response (200 OK):** Payment entity with status `REFUNDED`.

---

## 4. Merchant Service & API Keys (`/api/v1/merchants`)

### 4.1 Onboard Merchant
- **Method / Path:** `POST /api/v1/merchants`
- **Request Body:**
  ```json
  {
    "userId": "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
    "businessName": "Acme Payments Inc",
    "businessType": "E_COMMERCE"
  }
  ```

### 4.2 Generate Scoped API Key (Single Plaintext Exposure)
- **Method / Path:** `POST /api/v1/merchants/{merchantId}/api-keys`
- **Request Body:**
  ```json
  {
    "label": "Production Webhook Key"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "success": true,
    "data": {
      "id": "80f885d2-b287-4b08-b529-e26bc634cbb5",
      "merchantId": "a5bb0fa2-ed11-47be-9f6d-1c4da7142158",
      "label": "Production Webhook Key",
      "revoked": false,
      "rawKey": "pf_live_y7K3mPq8R1tV4wX9zB2dE5gH0jL6nS3vF8xQ1aC4eN7"
    }
  }
  ```
  *(Note: `rawKey` is `null` on all subsequent GET requests)*

---

## 5. Ledger & Double-Entry Journal (`/api/v1/ledger`)

### 5.1 Query Transaction Entries
- **Method / Path:** `GET /api/v1/ledger/transactions/{transactionId}`
- **Response (200 OK):**
  ```json
  {
    "success": true,
    "data": [
      {
        "id": "entry-01",
        "transactionId": "98765432-1234-5678-9abc-def012345678",
        "walletId": "11111111-1111-1111-1111-111111111111",
        "entryType": "DEBIT",
        "amountMinor": 25000,
        "currency": "INR"
      },
      {
        "id": "entry-02",
        "transactionId": "98765432-1234-5678-9abc-def012345678",
        "walletId": "22222222-2222-2222-2222-222222222222",
        "entryType": "CREDIT",
        "amountMinor": 25000,
        "currency": "INR"
      }
    ]
  }
  ```

---

## 6. Standard Error Response (RFC 7807 Aligned)

All error responses return a standardized JSON structure:

```json
{
  "success": false,
  "error": {
    "errorCode": "INSUFFICIENT_FUNDS",
    "message": "Wallet balance 1000 minor is insufficient for requested debit of 5000 minor",
    "status": 422,
    "path": "/api/v1/wallets/11111111-1111-1111-1111-111111111111/debit",
    "timestamp": "2026-08-31T01:05:00.123456Z",
    "details": {}
  },
  "traceId": "4f8e2cdaf442ca63"
}
```
