/**
 * PayFlow — Unified Developer Portal & Interactive API Hub
 * Autonomous Single Page Application Engine
 */

// ============================================================================
// 1. Mock State Storage & Realistic Fintech Simulation Engine
// ============================================================================

const MockDB = {
  currentUser: {
    userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
    email: "alice@payflow.com",
    firstName: "Alice",
    lastName: "Smith",
    phone: "+15551234567",
    role: "ROLE_USER",
    status: "ACTIVE"
  },
  wallets: {
    "11111111-1111-1111-1111-111111111111": {
      id: "11111111-1111-1111-1111-111111111111",
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      balanceMinor: 100000,
      currency: "INR",
      status: "ACTIVE",
      version: 4
    },
    "22222222-2222-2222-2222-222222222222": {
      id: "22222222-2222-2222-2222-222222222222",
      userId: "e9c7c570-dd4f-4f82-a4c8-9f6c75bebf16",
      balanceMinor: 50000,
      currency: "INR",
      status: "ACTIVE",
      version: 2
    }
  },
  payments: {
    "98765432-1234-5678-9abc-def012345678": {
      id: "98765432-1234-5678-9abc-def012345678",
      senderWalletId: "11111111-1111-1111-1111-111111111111",
      recipientWalletId: "22222222-2222-2222-2222-222222222222",
      amountMinor: 25000,
      currency: "INR",
      status: "SUCCESS",
      idempotencyKey: "PAY-TX-987654321",
      description: "Dinner split settlement",
      createdAt: "2026-08-31T01:05:00Z"
    }
  },
  merchants: {
    "a5bb0fa2-ed11-47be-9f6d-1c4da7142158": {
      id: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158",
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      businessName: "Acme Payments Inc",
      businessType: "E_COMMERCE",
      status: "ACTIVE",
      createdAt: "2026-08-31T01:00:00Z"
    }
  },
  apiKeys: [
    {
      id: "80f885d2-b287-4b08-b529-e26bc634cbb5",
      merchantId: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158",
      label: "Production Webhook Key",
      revoked: false,
      maskedKey: "pf_live_y7K3m...1aC4eN7",
      createdAt: "2026-08-31T01:02:00Z"
    }
  ],
  blacklists: [
    {
      id: "b1-0001",
      targetType: "IP_ADDRESS",
      targetValue: "198.51.100.24",
      reason: "Known credential stuffing origin",
      createdAt: "2026-08-31T00:30:00Z"
    }
  ],
  notifications: [
    {
      id: "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      type: "EMAIL",
      subject: "Payment Received - INR 250.00",
      content: "Your wallet received 250.00 INR from Alice Smith.",
      status: "DELIVERED",
      sentAt: "2026-08-31T01:05:02Z"
    }
  ]
};

// ============================================================================
// 2. Preset Role Tokens & Quick Auth
// ============================================================================

const PRESET_TOKENS = {
  user: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkOGI2YjQ2OS1jYzNlLTRlNzEtOTNiNy04ZjViNjRhZGFmMDUiLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzI1MTEwNDAwLCJleHAiOjE3MjUxMTEzMDB9.signature_demo_user",
  admin: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbi0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDAiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlhdCI6MTcyNTExMDQwMCwiZXhwIjoxNzI1MTExMzAwfQ.signature_demo_admin",
  merchant: "pf_live_y7K3mPq8R1tV4wX9zB2dE5gH0jL6nS3vF8xQ1aC4eN7"
};

// ============================================================================
// 3. Complete Endpoint Catalog (27 Endpoints)
// ============================================================================

const API_CATALOG = [
  // 1. Authentication
  {
    id: "auth-register",
    category: "1. Authentication",
    name: "Register Account",
    method: "POST",
    path: "/api/v1/auth/register",
    description: "Registers a new user account with BCrypt password hashing and returns JWT credentials.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      email: "alice@payflow.com",
      password: "Password123!",
      firstName: "Alice",
      lastName: "Smith",
      phone: "+15551234567"
    },
    mockHandler: (req) => ({
      status: 201,
      data: {
        success: true,
        data: {
          userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
          accessToken: PRESET_TOKENS.user,
          refreshToken: "rt_live_9f8e2cdaf442ca63866d7f6e1fdac",
          expiresIn: 900
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "auth-login",
    category: "1. Authentication",
    name: "Login & Token Issuance",
    method: "POST",
    path: "/api/v1/auth/login",
    description: "Authenticates user credentials and issues a signed access token with refresh token rotation.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      email: "alice@payflow.com",
      password: "Password123!"
    },
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
          accessToken: PRESET_TOKENS.user,
          refreshToken: "rt_live_9f8e2cdaf442ca63866d7f6e1fdac",
          expiresIn: 900
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "auth-refresh",
    category: "1. Authentication",
    name: "Rotate Refresh Token",
    method: "POST",
    path: "/api/v1/auth/refresh",
    description: "Performs single-use refresh token rotation, invalidating token families on reuse attempts.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      refreshToken: "rt_live_9f8e2cdaf442ca63866d7f6e1fdac"
    },
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
          accessToken: PRESET_TOKENS.user,
          refreshToken: "rt_live_" + Math.random().toString(36).substring(2),
          expiresIn: 900
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "auth-logout",
    category: "1. Authentication",
    name: "Revoke Session (Logout)",
    method: "POST",
    path: "/api/v1/auth/logout",
    description: "Revokes active refresh token from Redis token blacklist.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      refreshToken: "rt_live_9f8e2cdaf442ca63866d7f6e1fdac"
    },
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: "Logged out successfully",
        traceId: generateTraceId()
      }
    })
  },

  // 2. User Service
  {
    id: "user-me",
    category: "2. User Management",
    name: "Get Current Profile (/me)",
    method: "GET",
    path: "/api/v1/users/me",
    description: "Extracts identity from JWT SecurityContext and returns the authenticated user's profile.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: MockDB.currentUser,
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "user-get-by-id",
    category: "2. User Management",
    name: "Get User by ID",
    method: "GET",
    path: "/api/v1/users/{userId}",
    description: "Retrieves user details by ID with Spring Security @PreAuthorize IDOR checks.",
    authRequired: true,
    pathParams: [
      { name: "userId", default: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05", desc: "User UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          userId: req.pathParams.userId || MockDB.currentUser.userId,
          email: "alice@payflow.com",
          firstName: "Alice",
          lastName: "Smith",
          phone: "+15551234567",
          role: "ROLE_USER",
          status: "ACTIVE",
          createdAt: "2026-08-31T01:00:00Z"
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "user-update-status",
    category: "2. User Management",
    name: "Update User Status",
    method: "PATCH",
    path: "/api/v1/users/{userId}/status",
    description: "Admin endpoint to update account lifecycle status (ACTIVE, SUSPENDED, CLOSED).",
    authRequired: true,
    pathParams: [
      { name: "userId", default: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05", desc: "User UUID" }
    ],
    queryParams: [
      { name: "status", default: "ACTIVE", desc: "ACTIVE | SUSPENDED | CLOSED" }
    ],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          userId: req.pathParams.userId,
          status: req.queryParams.status || "ACTIVE",
          updatedAt: new Date().toISOString()
        },
        traceId: generateTraceId()
      }
    })
  },

  // 3. Digital Wallet Service
  {
    id: "wallet-create",
    category: "3. Digital Wallet Service",
    name: "Create Wallet",
    method: "POST",
    path: "/api/v1/wallets",
    description: "Provisions an initial digital wallet with 0 minor balance for the authenticated user.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      currency: "INR"
    },
    mockHandler: (req) => {
      const newWalletId = "w-" + Math.random().toString(36).substring(2, 10);
      return {
        status: 201,
        data: {
          success: true,
          data: {
            id: newWalletId,
            userId: req.body?.userId || "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
            balanceMinor: 0,
            currency: req.body?.currency || "INR",
            status: "ACTIVE",
            version: 0
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-get",
    category: "3. Digital Wallet Service",
    name: "Get Wallet Details",
    method: "GET",
    path: "/api/v1/wallets/{walletId}",
    description: "Returns wallet status, currency, and version for optimistic lock verification.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      return {
        status: 200,
        data: {
          success: true,
          data: w,
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-balance",
    category: "3. Digital Wallet Service",
    name: "Get Authoritative Balance",
    method: "GET",
    path: "/api/v1/wallets/{walletId}/balance",
    description: "Fetches authoritative balance in minor units (cents/paise) with Redis cache fallback.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      const formatted = (w.balanceMinor / 100).toLocaleString('en-US', { minimumFractionDigits: 2 }) + " " + w.currency;
      return {
        status: 200,
        data: {
          success: true,
          data: {
            walletId: w.id,
            balanceMinor: w.balanceMinor,
            balanceFormatted: formatted,
            currency: w.currency,
            status: w.status
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-topup",
    category: "3. Digital Wallet Service",
    name: "Top-Up Wallet",
    method: "POST",
    path: "/api/v1/wallets/{walletId}/top-up",
    description: "Credits wallet using atomic SQL update and emits WalletCredited event.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: {
      amount: 500.00,
      currency: "INR",
      referenceId: "TOPUP-2026-001"
    },
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      const addMinor = Math.round((req.body?.amount || 500) * 100);
      w.balanceMinor += addMinor;
      w.version++;
      return {
        status: 200,
        data: {
          success: true,
          data: {
            walletId: w.id,
            balanceMinor: w.balanceMinor,
            balanceFormatted: (w.balanceMinor / 100).toFixed(2) + " " + w.currency,
            currency: w.currency,
            status: w.status
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-withdraw",
    category: "3. Digital Wallet Service",
    name: "Withdraw Funds (Atomic Conditional)",
    method: "POST",
    path: "/api/v1/wallets/{walletId}/withdraw",
    description: "Executes conditional debit (WHERE balance_minor >= :amount) preventing negative balances.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: {
      amount: 250.00,
      currency: "INR",
      referenceId: "WITHDRAW-2026-001"
    },
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      const debitMinor = Math.round((req.body?.amount || 250) * 100);
      if (w.balanceMinor < debitMinor) {
        return {
          status: 422,
          data: {
            success: false,
            error: {
              errorCode: "INSUFFICIENT_FUNDS",
              message: `Wallet balance ${w.balanceMinor} minor is insufficient for requested debit of ${debitMinor} minor`,
              status: 422,
              path: `/api/v1/wallets/${w.id}/withdraw`,
              timestamp: new Date().toISOString()
            },
            traceId: generateTraceId()
          }
        };
      }
      w.balanceMinor -= debitMinor;
      w.version++;
      return {
        status: 200,
        data: {
          success: true,
          data: {
            walletId: w.id,
            balanceMinor: w.balanceMinor,
            balanceFormatted: (w.balanceMinor / 100).toFixed(2) + " " + w.currency,
            currency: w.currency,
            status: w.status
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-freeze",
    category: "3. Digital Wallet Service",
    name: "Freeze Wallet (Admin)",
    method: "POST",
    path: "/api/v1/wallets/{walletId}/freeze",
    description: "Locks wallet preventing all debit and transfer actions during fraud investigations.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      w.status = "FROZEN";
      return {
        status: 200,
        data: {
          success: true,
          data: w,
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "wallet-unfreeze",
    category: "3. Digital Wallet Service",
    name: "Unfreeze Wallet (Admin)",
    method: "POST",
    path: "/api/v1/wallets/{walletId}/unfreeze",
    description: "Restores frozen wallet to ACTIVE status.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => {
      const w = MockDB.wallets[req.pathParams.walletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      w.status = "ACTIVE";
      return {
        status: 200,
        data: {
          success: true,
          data: w,
          traceId: generateTraceId()
        }
      };
    }
  },

  // 4. Payment & Saga Orchestrator
  {
    id: "payment-initiate",
    category: "4. Payment & Saga Orchestration",
    name: "Initiate Payment / P2P Transfer",
    method: "POST",
    path: "/api/v1/payments",
    description: "Orchestrates multi-service payment saga: Redis lock -> Debit -> Credit -> Double-entry Ledger -> Kafka Outbox.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      senderWalletId: "11111111-1111-1111-1111-111111111111",
      recipientWalletId: "22222222-2222-2222-2222-222222222222",
      amount: 250.00,
      currency: "INR",
      paymentType: "P2P_TRANSFER",
      idempotencyKey: "PAY-TX-987654321",
      description: "Dinner split settlement"
    },
    mockHandler: (req) => {
      const amountMinor = Math.round((req.body?.amount || 250) * 100);
      const senderW = MockDB.wallets[req.body?.senderWalletId] || MockDB.wallets["11111111-1111-1111-1111-111111111111"];
      const recipientW = MockDB.wallets[req.body?.recipientWalletId] || MockDB.wallets["22222222-2222-2222-2222-222222222222"];
      
      if (senderW.balanceMinor < amountMinor) {
        return {
          status: 422,
          data: {
            success: false,
            error: {
              errorCode: "INSUFFICIENT_FUNDS",
              message: "Sender wallet balance is insufficient for transfer.",
              status: 422,
              path: "/api/v1/payments"
            },
            traceId: generateTraceId()
          }
        };
      }

      senderW.balanceMinor -= amountMinor;
      recipientW.balanceMinor += amountMinor;

      const txId = "tx-" + Math.random().toString(36).substring(2, 10);
      const paymentRecord = {
        id: txId,
        senderWalletId: senderW.id,
        recipientWalletId: recipientW.id,
        amountMinor: amountMinor,
        currency: req.body?.currency || "INR",
        status: "SUCCESS",
        idempotencyKey: req.body?.idempotencyKey || "PAY-TX-AUTO",
        createdAt: new Date().toISOString()
      };
      MockDB.payments[txId] = paymentRecord;

      return {
        status: 201,
        data: {
          success: true,
          data: paymentRecord,
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "payment-get",
    category: "4. Payment & Saga Orchestration",
    name: "Get Payment Details",
    method: "GET",
    path: "/api/v1/payments/{paymentId}",
    description: "Queries payment state machine status and transactional audit details.",
    authRequired: true,
    pathParams: [
      { name: "paymentId", default: "98765432-1234-5678-9abc-def012345678", desc: "Payment UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => {
      const p = MockDB.payments[req.pathParams.paymentId] || MockDB.payments["98765432-1234-5678-9abc-def012345678"];
      return {
        status: 200,
        data: {
          success: true,
          data: p,
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "payment-refund",
    category: "4. Payment & Saga Orchestration",
    name: "Refund Payment",
    method: "POST",
    path: "/api/v1/payments/{paymentId}/refund",
    description: "Executes backward compensating transactions, returning funds to sender and marking status REFUNDED.",
    authRequired: true,
    pathParams: [
      { name: "paymentId", default: "98765432-1234-5678-9abc-def012345678", desc: "Payment UUID" }
    ],
    queryParams: [],
    defaultBody: {
      reason: "Customer requested cancellation"
    },
    mockHandler: (req) => {
      const p = MockDB.payments[req.pathParams.paymentId] || MockDB.payments["98765432-1234-5678-9abc-def012345678"];
      p.status = "REFUNDED";
      return {
        status: 200,
        data: {
          success: true,
          data: p,
          traceId: generateTraceId()
        }
      };
    }
  },

  // 5. Ledger & Double-Entry Journal
  {
    id: "ledger-record-entry",
    category: "5. Ledger & Double-Entry Journal",
    name: "Record Balanced Journal Entry",
    method: "POST",
    path: "/api/v1/ledger/entries",
    description: "Records immutable journal entry verifying Sum(Debits) == Sum(Credits) to guarantee zero drift.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      transactionId: "98765432-1234-5678-9abc-def012345678",
      reference: "SETTLE-2026-001",
      lines: [
        {
          walletId: "11111111-1111-1111-1111-111111111111",
          entryType: "DEBIT",
          amountMinor: 25000,
          currency: "INR"
        },
        {
          walletId: "22222222-2222-2222-2222-222222222222",
          entryType: "CREDIT",
          amountMinor: 25000,
          currency: "INR"
        }
      ]
    },
    mockHandler: (req) => ({
      status: 201,
      data: {
        success: true,
        data: {
          id: "jnl-" + Math.random().toString(36).substring(2, 10),
          transactionId: req.body?.transactionId || "98765432-1234-5678-9abc-def012345678",
          totalDebitMinor: 25000,
          totalCreditMinor: 25000,
          currency: "INR",
          createdAt: new Date().toISOString(),
          lines: req.body?.lines || []
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "ledger-get-tx",
    category: "5. Ledger & Double-Entry Journal",
    name: "Get Journal by Transaction ID",
    method: "GET",
    path: "/api/v1/ledger/transactions/{transactionId}",
    description: "Returns full double-entry postings for audit trail verification.",
    authRequired: true,
    pathParams: [
      { name: "transactionId", default: "98765432-1234-5678-9abc-def012345678", desc: "Transaction UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          id: "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          transactionId: req.pathParams.transactionId,
          totalDebitMinor: 25000,
          totalCreditMinor: 25000,
          currency: "INR",
          createdAt: "2026-08-31T01:05:01Z",
          lines: [
            { id: "line-01", walletId: "11111111-1111-1111-1111-111111111111", entryType: "DEBIT", amountMinor: 25000 },
            { id: "line-02", walletId: "22222222-2222-2222-2222-222222222222", entryType: "CREDIT", amountMinor: 25000 }
          ]
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "ledger-get-statement",
    category: "5. Ledger & Double-Entry Journal",
    name: "Get Wallet Statement (Paginated)",
    method: "GET",
    path: "/api/v1/ledger/wallets/{walletId}",
    description: "Returns paginated double-entry debit/credit ledger statement for a specific wallet.",
    authRequired: true,
    pathParams: [
      { name: "walletId", default: "11111111-1111-1111-1111-111111111111", desc: "Wallet UUID" }
    ],
    queryParams: [
      { name: "page", default: "0", desc: "Page index" },
      { name: "size", default: "20", desc: "Page size" }
    ],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          content: [
            { id: "line-01", transactionId: "98765432-1234-5678-9abc-def012345678", entryType: "DEBIT", amountMinor: 25000, currency: "INR", timestamp: "2026-08-31T01:05:00Z" },
            { id: "line-00", transactionId: "11223344-1234-5678-9abc-def012345678", entryType: "CREDIT", amountMinor: 100000, currency: "INR", timestamp: "2026-08-31T01:00:00Z" }
          ],
          pageable: { pageNumber: 0, pageSize: 20 },
          totalElements: 2,
          totalPages: 1
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "ledger-audit-verify",
    category: "5. Ledger & Double-Entry Journal",
    name: "Verify Double-Entry Integrity",
    method: "GET",
    path: "/api/v1/ledger/audit/verify",
    description: "Executes mathematical audit across all accounts: checks Sum(Debits) - Sum(Credits) == 0 (zero drift).",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: {
          status: "BALANCED",
          totalDebitsMinor: 150000000,
          totalCreditsMinor: 150000000,
          driftMinor: 0,
          accountsAudited: 1420,
          verifiedAt: new Date().toISOString()
        },
        traceId: generateTraceId()
      }
    })
  },

  // 6. Merchant Service
  {
    id: "merchant-register",
    category: "6. Merchant & API Keys",
    name: "Register Merchant Profile",
    method: "POST",
    path: "/api/v1/merchants",
    description: "Onboards business merchant with verification status.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      businessName: "Acme Payments Inc",
      businessType: "E_COMMERCE"
    },
    mockHandler: (req) => ({
      status: 201,
      data: {
        success: true,
        data: {
          id: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158",
          userId: req.body?.userId || "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
          businessName: req.body?.businessName || "Acme Payments Inc",
          businessType: req.body?.businessType || "E_COMMERCE",
          status: "ACTIVE",
          createdAt: new Date().toISOString()
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "merchant-get",
    category: "6. Merchant & API Keys",
    name: "Get Merchant Details",
    method: "GET",
    path: "/api/v1/merchants/{merchantId}",
    description: "Retrieves merchant profile and status.",
    authRequired: true,
    pathParams: [
      { name: "merchantId", default: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158", desc: "Merchant UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: MockDB.merchants[req.pathParams.merchantId] || MockDB.merchants["a5bb0fa2-ed11-47be-9f6d-1c4da7142158"],
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "merchant-create-key",
    category: "6. Merchant & API Keys",
    name: "Generate API Key (One-time Raw Secret)",
    method: "POST",
    path: "/api/v1/merchants/{merchantId}/api-keys",
    description: "Generates CSPRNG API key prefixed with pf_live_. Returns rawKey only once, storing SHA-256 hash.",
    authRequired: true,
    pathParams: [
      { name: "merchantId", default: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158", desc: "Merchant UUID" }
    ],
    queryParams: [],
    defaultBody: {
      label: "Production Webhook Key"
    },
    mockHandler: (req) => {
      const generatedRawKey = "pf_live_" + Math.random().toString(36).substring(2) + Math.random().toString(36).substring(2);
      const newKeyId = "key-" + Math.random().toString(36).substring(2, 10);
      return {
        status: 201,
        data: {
          success: true,
          data: {
            id: newKeyId,
            merchantId: req.pathParams.merchantId,
            label: req.body?.label || "Production Key",
            revoked: false,
            rawKey: generatedRawKey,
            createdAt: new Date().toISOString()
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "merchant-list-keys",
    category: "6. Merchant & API Keys",
    name: "List Merchant API Keys",
    method: "GET",
    path: "/api/v1/merchants/{merchantId}/api-keys",
    description: "Lists all merchant API keys. Note: rawKey is null on all GET queries.",
    authRequired: true,
    pathParams: [
      { name: "merchantId", default: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158", desc: "Merchant UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: MockDB.apiKeys,
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "merchant-revoke-key",
    category: "6. Merchant & API Keys",
    name: "Revoke API Key",
    method: "DELETE",
    path: "/api/v1/merchants/{merchantId}/api-keys/{keyId}",
    description: "Revokes merchant API key immediately.",
    authRequired: true,
    pathParams: [
      { name: "merchantId", default: "a5bb0fa2-ed11-47be-9f6d-1c4da7142158", desc: "Merchant UUID" },
      { name: "keyId", default: "80f885d2-b287-4b08-b529-e26bc634cbb5", desc: "API Key UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: null,
        traceId: generateTraceId()
      }
    })
  },

  // 7. Fraud & Risk Engine
  {
    id: "fraud-evaluate",
    category: "7. Fraud & Risk Engine",
    name: "Evaluate Transaction Risk",
    method: "POST",
    path: "/api/v1/fraud/evaluate",
    description: "Evaluates transaction velocity sliding-window rules, IP blacklists, and threshold risk scores.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      walletId: "11111111-1111-1111-1111-111111111111",
      amount: 250.00,
      currency: "INR",
      ipAddress: "192.168.1.50"
    },
    mockHandler: (req) => {
      const amt = req.body?.amount || 250;
      const decision = amt > 50000 ? "REJECTED" : (amt > 10000 ? "FLAGGED" : "APPROVED");
      const score = amt > 50000 ? 92 : (amt > 10000 ? 55 : 12);
      return {
        status: 200,
        data: {
          success: true,
          data: {
            decision: decision,
            riskScore: score,
            reasons: decision === "APPROVED" ? [] : ["High velocity transfer threshold exceeded"]
          },
          traceId: generateTraceId()
        }
      };
    }
  },
  {
    id: "fraud-blacklist",
    category: "7. Fraud & Risk Engine",
    name: "Add to Blacklist (Admin)",
    method: "POST",
    path: "/api/v1/fraud/blacklist",
    description: "Adds suspicious IP address, User ID, or Wallet ID to active blocking registry.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      targetType: "IP_ADDRESS",
      targetValue: "198.51.100.24",
      reason: "Known credential stuffing origin"
    },
    mockHandler: (req) => ({
      status: 201,
      data: {
        success: true,
        data: {
          id: "bl-" + Math.random().toString(36).substring(2, 8),
          targetType: req.body?.targetType || "IP_ADDRESS",
          targetValue: req.body?.targetValue || "198.51.100.24",
          reason: req.body?.reason || "Security violation",
          createdAt: new Date().toISOString()
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "fraud-flagged",
    category: "7. Fraud & Risk Engine",
    name: "Get Flagged Transactions",
    method: "GET",
    path: "/api/v1/fraud/flagged/{userId}",
    description: "Queries high-risk transactions flagged for manual review.",
    authRequired: true,
    pathParams: [
      { name: "userId", default: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05", desc: "User UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: [
          {
            id: "flag-01",
            userId: req.pathParams.userId,
            amountMinor: 2500000,
            riskScore: 68,
            status: "UNDER_REVIEW",
            createdAt: "2026-08-31T00:50:00Z"
          }
        ],
        traceId: generateTraceId()
      }
    })
  },

  // 8. Notification Service
  {
    id: "notif-send",
    category: "8. Notification Service",
    name: "Dispatch Notification",
    method: "POST",
    path: "/api/v1/notifications/send",
    description: "Dispatches multi-channel notification (Email, SMS, Push) using Strategy Pattern.",
    authRequired: true,
    pathParams: [],
    queryParams: [],
    defaultBody: {
      userId: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
      type: "EMAIL",
      subject: "Payment Received - INR 250.00",
      content: "Your wallet received 250.00 INR from Alice Smith."
    },
    mockHandler: (req) => ({
      status: 201,
      data: {
        success: true,
        data: {
          id: "notif-" + Math.random().toString(36).substring(2, 8),
          userId: req.body?.userId || "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05",
          type: req.body?.type || "EMAIL",
          status: "DELIVERED",
          sentAt: new Date().toISOString()
        },
        traceId: generateTraceId()
      }
    })
  },
  {
    id: "notif-history",
    category: "8. Notification Service",
    name: "Get Notification History",
    method: "GET",
    path: "/api/v1/notifications/{userId}",
    description: "Returns chronological history of user dispatched notifications.",
    authRequired: true,
    pathParams: [
      { name: "userId", default: "d8b6b469-cc3e-4e71-93b7-8f5b64adaf05", desc: "User UUID" }
    ],
    queryParams: [],
    defaultBody: null,
    mockHandler: (req) => ({
      status: 200,
      data: {
        success: true,
        data: MockDB.notifications,
        traceId: generateTraceId()
      }
    })
  },

  // 9. Actuator & Observability
  {
    id: "actuator-health",
    category: "9. System & Observability",
    name: "Cluster Health Status",
    method: "GET",
    path: "/actuator/health",
    description: "Actuator health indicator reporting PostgreSQL, Redis, and Kafka cluster connectivity.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: null,
    mockHandler: () => ({
      status: 200,
      data: {
        status: "UP",
        components: {
          db: { status: "UP", details: { database: "PostgreSQL 16", connections: 18 } },
          redis: { status: "UP", details: { version: "7.2.4", ping: "PONG" } },
          kafka: { status: "UP", details: { clusterId: "payflow-kafka-cluster" } },
          diskSpace: { status: "UP", details: { free: 42949672960 } }
        }
      }
    })
  },
  {
    id: "actuator-metrics",
    category: "9. System & Observability",
    name: "Micrometer Metrics Catalog",
    method: "GET",
    path: "/actuator/metrics",
    description: "Lists all registered Micrometer timers, counters, and percentiles.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: null,
    mockHandler: () => ({
      status: 200,
      data: {
        names: [
          "payflow.payment.latency",
          "payflow.payment.throughput",
          "payflow.saga.compensation.total",
          "payflow.idempotency.hits",
          "jvm.memory.used",
          "system.cpu.usage",
          "http.server.requests"
        ]
      }
    })
  },
  {
    id: "actuator-prometheus",
    category: "9. System & Observability",
    name: "Prometheus Scrape Text",
    method: "GET",
    path: "/actuator/prometheus",
    description: "Exposes Prometheus text metric exposition format for scraping.",
    authRequired: false,
    pathParams: [],
    queryParams: [],
    defaultBody: null,
    mockHandler: () => ({
      status: 200,
      data: `# HELP payflow_payment_latency_seconds Latency percentiles
# TYPE payflow_payment_latency_seconds summary
payflow_payment_latency_seconds{quantile="0.5"} 0.0082
payflow_payment_latency_seconds{quantile="0.95"} 0.0145
payflow_payment_latency_seconds{quantile="0.99"} 0.0221
# HELP payflow_saga_compensation_total Total compensating refunds
# TYPE payflow_saga_compensation_total counter
payflow_saga_compensation_total 0`
    })
  }
];

// ============================================================================
// 4. Architectural Decision Records (ADRs) Catalog
// ============================================================================

const ADR_CATALOG = [
  {
    id: "ADR-001",
    title: "Saga Orchestration vs. Choreography for Multi-Service Payments",
    category: "Messaging",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "Executing a financial transfer requires coordinating operations across multiple microservices (Wallet debit, Wallet credit, Ledger journal booking, Fraud evaluation). We required a pattern that ensures strict ACID-adjacent consistency, deterministic compensation on failure, and complete audit visibility.",
    alternatives: [
      {
        option: "Saga Orchestration (Centralized State Machine)",
        pros: "Explicit FSM state visibility, deterministic backward compensation sequencing, zero cyclic dependencies, simplified operational triage.",
        cons: "Slightly higher orchestrator coupling; orchestrator requires high-availability failover."
      },
      {
        option: "Saga Choreography (Event-Driven Reactive)",
        pros: "Loose coupling between services; no single orchestrator node.",
        cons: "Difficult to track end-to-end transaction state; cyclic failure cascade hazards; hard to test complex compensating refund paths."
      },
      {
        option: "Two-Phase Commit (2PC / XA)",
        pros: "Strict synchronous ACID.",
        cons: "Blocking coordinator locks degrade throughput catastrophically in distributed microservices; vulnerable to network partitions."
      }
    ],
    decision: "Adopted **Centralized Saga Orchestrator** in `payment-service` with an explicit state machine (`CREATED → PROCESSING → SUCCESS / FAILED → REFUNDED`). If the credit leg fails, the orchestrator triggers deterministic backward compensating refund (`creditSenderRefund`).",
    tradeoffs: "Orchestrator state is persisted before each step in `payment_db` alongside outbox events, guaranteeing safe recovery on JVM crash via `SagaReconciliationJob`.",
    codeRef: "payment-service/src/main/java/com/payflow/payment/service/PaymentServiceImpl.java"
  },
  {
    id: "ADR-002",
    title: "Two-Tier Distributed Idempotency Defense",
    category: "Security",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "Fintech clients frequently retry payments on network timeouts or double-click buttons. Duplicate debits result in regulatory violations and financial loss.",
    alternatives: [
      {
        option: "Two-Tier Defense (Redis Lock + DB Unique Constraint)",
        pros: "Sub-millisecond rejection of concurrent in-flight retries in Redis; absolute guarantee against duplicates via DB composite constraint.",
        cons: "Requires synchronizing Redis TTL with DB transaction lifetime."
      },
      {
        option: "Database Unique Constraint Only",
        pros: "Simple, single source of truth.",
        cons: "Spike load hits PostgreSQL connection pools; error handling requires parsing SQL vendor codes."
      },
      {
        option: "Redis-Only Token Storage",
        pros: "Fastest performance.",
        cons: "Data loss during Redis node restarts or eviction leads to duplicate financial debits."
      }
    ],
    decision: "Implemented **Two-Tier Idempotency**: Tier 1 acquires a fast-path distributed lock in Redis `idempotency:lock:{walletId}:{key}` with 10s TTL. Tier 2 enforces composite unique constraint `(sender_wallet_id, idempotency_key)` in PostgreSQL `payments` table.",
    tradeoffs: "If Redis is unavailable, the system safely falls back directly to the PostgreSQL unique constraint.",
    codeRef: "payment-service/src/main/java/com/payflow/payment/idempotency/IdempotencyAspect.java"
  },
  {
    id: "ADR-003",
    title: "Transactional Outbox & Inbox Pattern for Event Publishing",
    category: "Resilience",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "When updating PostgreSQL state (e.g., Payment SUCCESS) and publishing events to Apache Kafka (`payment.completed`), direct dual-writes create inconsistent states if either write fails.",
    alternatives: [
      {
        option: "Transactional Outbox & Inbox",
        pros: "Eliminates dual-write hazard; ACID transaction saves business entity and event in same commit; consumers deduplicate via processed_events table.",
        cons: "Introduces polling latency (~50ms) between DB commit and Kafka emit."
      },
      {
        option: "Direct Kafka Publish inside @Transactional",
        pros: "Immediate publication.",
        cons: "Critical dual-write bug: If Kafka publish succeeds but DB transaction rolls back, downstream systems process phantom payments."
      },
      {
        option: "Kafka Transactions (Chained Transaction Manager)",
        pros: "Atomic commit across DB and Kafka.",
        cons: "High complexity; significant latency overhead; poor driver interoperability across microservices."
      }
    ],
    decision: "Adopted **Transactional Outbox Table** in all producing services paired with an asynchronous `OutboxPoller` (`acks=all`), and **Inbox Table (`processed_events`)** on consumer services.",
    tradeoffs: "Outbox poller runs on a scheduled fixed-delay executor with exponential backoff on Kafka broker connectivity hiccups.",
    codeRef: "payment-service/src/main/java/com/payflow/payment/outbox/OutboxPoller.java"
  },
  {
    id: "ADR-004",
    title: "Minor Unit Integer Accounting (amountMinor) vs. Floating-Point",
    category: "Data",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "Binary floating point types (`float`, `double`) cannot represent decimal fractions precisely (e.g. 0.1 + 0.2 = 0.30000000000000004), introducing balance drift over time.",
    alternatives: [
      {
        option: "Primitive long Minor Units (e.g., 100000L = 1,000.00 INR)",
        pros: "Zero floating-point rounding errors; ultra-fast CPU atomic arithmetic; compact BIGINT storage; natural compatibility with fintech standards.",
        cons: "Requires explicit minor-to-major conversion at REST API boundaries."
      },
      {
        option: "BigDecimal Throughout Entire System",
        pros: "Arbitrary precision; built-in rounding modes.",
        cons: "High memory allocation overhead; slow object comparisons; awkward SQL mapping."
      },
      {
        option: "Double / Float",
        pros: "Native primitive types.",
        cons: "Catastrophic financial rounding errors and balance drift."
      }
    ],
    decision: "All database columns and internal domain models use `long amountMinor`. `BigDecimal` is strictly used at REST DTO boundaries with explicit validation before integer conversion.",
    tradeoffs: "Sub-cent fractions are not supported, which is the standard for non-crypto retail fiat currencies.",
    codeRef: "wallet-service/src/main/java/com/payflow/wallet/domain/entity/Wallet.java"
  },
  {
    id: "ADR-005",
    title: "Atomic Conditional SQL Updates vs. Pessimistic Row Locking",
    category: "Data",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "High-concurrency transfers against the same wallet can cause race conditions and negative balances without synchronization.",
    alternatives: [
      {
        option: "Atomic Conditional Updates (WHERE balance_minor >= :amount)",
        pros: "No lock holding time; non-blocking reads; instant detection via updated row count; zero deadlock danger.",
        cons: "Requires optimistic retry handling on high concurrent write contention."
      },
      {
        option: "Pessimistic Locking (SELECT ... FOR UPDATE)",
        pros: "Guaranteed serialization of wallet updates.",
        cons: "Degrades throughput under load; high risk of distributed lock deadlocks when acquiring multiple wallet rows."
      }
    ],
    decision: "Adopted **Atomic Conditional SQL Updates** with version bumping: `UPDATE wallets SET balance_minor = balance_minor - :amount, version = version + 1 WHERE id = :walletId AND balance_minor >= :amount`.",
    tradeoffs: "If updated rows == 0, the service immediately distinguishes between insufficient funds and concurrent version conflicts.",
    codeRef: "wallet-service/src/main/java/com/payflow/wallet/repository/WalletRepository.java"
  },
  {
    id: "ADR-006",
    title: "One-Time Plaintext CSPRNG API Keys with SHA-256 Hashes",
    category: "Security",
    status: "ACCEPTED",
    date: "2026-08-31",
    context: "Merchant API keys grant programmatic payment capabilities. Storing plaintext keys in the database poses catastrophic data breach risks.",
    alternatives: [
      {
        option: "CSPRNG Generated + Single Plaintext Exposure + SHA-256 Storage",
        pros: "Industry standard (Stripe model); database breaches never reveal live secrets; raw key never retrievable on subsequent GETs.",
        cons: "Merchants must store their key immediately upon generation."
      },
      {
        option: "Reversible AES-256 Symmetric Encryption in DB",
        pros: "Allows revealing the key later in the dashboard.",
        cons: "Compromise of the master encryption key compromises all merchant credentials."
      }
    ],
    decision: "API keys are generated via `SecureRandom` with prefix `pf_live_` for automated secret scanning, and stored **exclusively as SHA-256 hashes**. The plaintext is returned only in the 201 Created response.",
    tradeoffs: "If a merchant loses their API key, it must be revoked and regenerated.",
    codeRef: "merchant-service/src/main/java/com/payflow/merchant/service/MerchantServiceImpl.java"
  }
];

// ============================================================================
// 5. Sequence Flows Catalog
// ============================================================================

const FLOWS_CATALOG = {
  saga: {
    title: "P2P Payment Transfer (Saga & Backward Compensation)",
    steps: [
      {
        num: 1,
        title: "Ingress & Idempotency Lock",
        service: "API Gateway & Payment Service",
        desc: "Client sends POST /api/v1/payments with Idempotency-Key. Orchestrator acquires fast-path Redis lock on key 'idempotency:lock:{senderWalletId}:{key}' (10s TTL)."
      },
      {
        num: 2,
        title: "Atomic Sender Debit Leg",
        service: "Wallet Service",
        desc: "Orchestrator calls Wallet Service to debit sender. Conditional SQL 'UPDATE wallets SET balance = balance - 25000 WHERE id = :sender AND balance >= 25000' executes atomically."
      },
      {
        num: 3,
        title: "Recipient Credit Leg & Compensation Branch",
        service: "Wallet Service",
        desc: "Orchestrator calls Wallet Service to credit recipient. If recipient wallet is FROZEN or closed, backward compensation immediately executes 'creditSenderRefund', restoring sender funds."
      },
      {
        num: 4,
        title: "Double-Entry Ledger Booking",
        service: "Ledger Service",
        desc: "Orchestrator calls Ledger Service to record balanced double-entry journal entry: Debit Sender Wallet (25000) + Credit Recipient Wallet (25000)."
      },
      {
        num: 5,
        title: "Atomic Outbox Enqueue & Kafka Streaming",
        service: "Payment Service & Apache Kafka",
        desc: "Payment status set to SUCCESS and 'PaymentCompleted' CloudEvent saved in 'outbox_events' table in single ACID transaction. OutboxPoller publishes to Kafka topic 'payment.completed'."
      },
      {
        num: 6,
        title: "Asynchronous Notifications Dispatch",
        service: "Notification Service",
        desc: "Notification Service Kafka listener consumes 'payment.completed' event, checks idempotency via inbox table, and sends multi-channel Email & Push alerts."
      }
    ]
  },
  ledger: {
    title: "Double-Entry Ledger Integrity Audit",
    steps: [
      {
        num: 1,
        title: "Audit Trigger",
        service: "Ledger Service (Admin API)",
        desc: "Scheduled reconciliation cron or Admin triggers GET /api/v1/ledger/audit/verify."
      },
      {
        num: 2,
        title: "Aggregate Debit & Credit Summation",
        service: "Ledger Service & PostgreSQL",
        desc: "Executes aggregate query across all journal entry line items: 'SELECT SUM(CASE WHEN entry_type = 'DEBIT' THEN amount_minor ELSE 0 END), SUM(CASE WHEN entry_type = 'CREDIT' THEN amount_minor ELSE 0 END) FROM journal_lines'."
      },
      {
        num: 3,
        title: "Drift Verification & Mathematical Proof",
        service: "Ledger Service",
        desc: "Calculates mathematical drift: Drift = TotalDebits - TotalCredits. Validates Drift === 0. Emits Prometheus metric 'payflow_ledger_drift_minor'."
      }
    ]
  },
  outbox: {
    title: "Transactional Outbox & Inbox Event Streaming",
    steps: [
      {
        num: 1,
        title: "Domain State Change & Outbox Write",
        service: "Originating Service (e.g., Payment)",
        desc: "Inside a single Spring @Transactional boundary, entity table is updated and event JSON is inserted into 'outbox_events' table."
      },
      {
        num: 2,
        title: "Outbox Polling & Kafka Emission",
        service: "Outbox Poller",
        desc: "OutboxPoller reads unpublished records with SELECT FOR UPDATE SKIP LOCKED, publishes to Kafka with acks=all, and marks record PUBLISHED."
      },
      {
        num: 3,
        title: "Consumer Inbox Deduplication",
        service: "Consuming Service (e.g., Notification)",
        desc: "Consumer receives message, checks 'processed_events' table with primary key (event_id, consumer_name). If duplicate, message is safely acknowledged and discarded."
      }
    ]
  },
  apikey: {
    title: "Merchant Onboarding & CSPRNG API Key Ingestion",
    steps: [
      {
        num: 1,
        title: "Key Generation via CSPRNG",
        service: "Merchant Service",
        desc: "Merchant triggers POST /api/v1/merchants/{id}/api-keys. System generates 256-bit cryptographically secure string with prefix 'pf_live_'."
      },
      {
        num: 2,
        title: "SHA-256 Hashing & Storage",
        service: "Merchant Service & PostgreSQL",
        desc: "Calculates SHA-256 digest of plaintext key. Persists hash, key prefix, label, and merchantId in database. Discards plaintext from memory."
      },
      {
        num: 3,
        title: "Single Plaintext Exposure Response",
        service: "Developer Portal / Client",
        desc: "Returns raw plaintext key in 201 Created response. Subsequent GET /api-keys returns rawKey as null."
      }
    ]
  }
};

// ============================================================================
// 6. Application State & Controller
// ============================================================================

let currentEndpoint = API_CATALOG[0];
let currentEnvironment = "mock"; // 'mock' | 'live'
let liveBaseUrl = "http://localhost:8080";
let activeAuthToken = PRESET_TOKENS.user;
let activeLangTab = "curl";

// Helper: Generate 16-hex Trace ID
function generateTraceId() {
  return Array.from({ length: 16 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
}

// Helper: Format JSON with syntax coloring
function formatJsonWithHighlight(jsonObj) {
  if (typeof jsonObj !== 'string') {
    jsonObj = JSON.stringify(jsonObj, null, 2);
  }
  return jsonObj
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
      let cls = 'json-number';
      if (/^"/.test(match)) {
        if (/:$/.test(match)) {
          cls = 'json-key';
        } else {
          cls = 'json-string';
        }
      } else if (/true|false/.test(match)) {
        cls = 'json-boolean';
      } else if (/null/.test(match)) {
        cls = 'json-null';
      }
      return '<span class="' + cls + '">' + match + '</span>';
    });
}

// Toast notification trigger
function showToast(message) {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.innerHTML = `
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
    <span>${message}</span>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 200);
  }, 2400);
}

// ============================================================================
// 7. Dynamic Code Snippet Generators
// ============================================================================

function generateCodeSnippets(endpoint, pathParams, queryParams, body, token) {
  let effectivePath = endpoint.path;
  endpoint.pathParams.forEach(p => {
    const val = pathParams[p.name] || p.default;
    effectivePath = effectivePath.replace(`{${p.name}}`, encodeURIComponent(val));
  });

  const queryEntries = Object.entries(queryParams).filter(([_, v]) => v !== "");
  const queryString = queryEntries.length > 0 ? "?" + new URLSearchParams(queryParams).toString() : "";
  const host = currentEnvironment === "live" ? liveBaseUrl : "https://api.payflow.com";
  const fullUrl = host + effectivePath + queryString;

  const headers = [];
  if (endpoint.method !== "GET" && body) {
    headers.push("-H 'Content-Type: application/json'");
  }
  if (endpoint.authRequired && token) {
    if (token.startsWith("pf_live_")) {
      headers.push(`-H 'X-API-Key: ${token}'`);
    } else {
      headers.push(`-H 'Authorization: Bearer ${token}'`);
    }
  }

  // 1. cURL
  let curlCode = `curl -X ${endpoint.method} '${fullUrl}' \\\n  ` + headers.join(" \\\n  ");
  if (endpoint.method !== "GET" && body) {
    curlCode += ` \\\n  -d '${JSON.stringify(body, null, 2)}'`;
  }

  // 2. JavaScript (Fetch)
  const fetchHeadersObj = {};
  if (endpoint.method !== "GET" && body) fetchHeadersObj["Content-Type"] = "application/json";
  if (endpoint.authRequired && token) {
    if (token.startsWith("pf_live_")) fetchHeadersObj["X-API-Key"] = token;
    else fetchHeadersObj["Authorization"] = `Bearer ${token}`;
  }

  const jsCode = `const response = await fetch('${fullUrl}', {
  method: '${endpoint.method}',
  headers: ${JSON.stringify(fetchHeadersObj, null, 4)}${endpoint.method !== "GET" && body ? `,\n  body: JSON.stringify(${JSON.stringify(body, null, 4)})` : ""}
});
const data = await response.json();
console.log(data);`;

  // 3. Python (requests)
  const pyHeaders = {};
  if (endpoint.authRequired && token) {
    if (token.startsWith("pf_live_")) pyHeaders["X-API-Key"] = token;
    else pyHeaders["Authorization"] = `Bearer ${token}`;
  }
  const pyCode = `import requests

url = "${fullUrl}"
headers = ${JSON.stringify(pyHeaders, null, 4)}
${endpoint.method !== "GET" && body ? `payload = ${JSON.stringify(body, null, 4)}\n\nresponse = requests.${endpoint.method.toLowerCase()}(url, headers=headers, json=payload)` : `response = requests.${endpoint.method.toLowerCase()}(url, headers=headers)`}

print(response.status_code)
print(response.json())`;

  // 4. Java (HttpClient)
  const javaCode = `import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PayFlowClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("${fullUrl}"))
            .header("Content-Type", "application/json")
            ${endpoint.authRequired && token ? `.header("Authorization", "Bearer ${token}")\n            ` : ""}.${endpoint.method}(${endpoint.method !== "GET" && body ? `HttpRequest.BodyPublishers.ofString("""\n${JSON.stringify(body, null, 4)}\n""")` : `HttpRequest.BodyPublishers.noBody()`})
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
}`;

  return {
    curl: curlCode,
    javascript: jsCode,
    python: pyCode,
    java: javaCode
  };
}

// ============================================================================
// 8. Render Engine & UI Handlers
// ============================================================================

function renderSidebar(filterText = "") {
  const container = document.getElementById('servicesList');
  if (!container) return;
  container.innerHTML = "";

  const grouped = {};
  API_CATALOG.forEach(ep => {
    if (filterText) {
      const match = ep.name.toLowerCase().includes(filterText.toLowerCase()) ||
                    ep.path.toLowerCase().includes(filterText.toLowerCase()) ||
                    ep.category.toLowerCase().includes(filterText.toLowerCase());
      if (!match) return;
    }
    if (!grouped[ep.category]) grouped[ep.category] = [];
    grouped[ep.category].push(ep);
  });

  for (const [catName, endpoints] of Object.entries(grouped)) {
    const catGroup = document.createElement('div');
    catGroup.className = 'service-category';
    catGroup.innerHTML = `<div class="category-header"><span>${catName}</span><span>${endpoints.length}</span></div>`;

    endpoints.forEach(ep => {
      const btn = document.createElement('button');
      btn.className = `endpoint-btn ${ep.id === currentEndpoint.id ? 'active' : ''}`;
      btn.innerHTML = `
        <div class="endpoint-btn-content">
          <span class="badge-method ${ep.method.toLowerCase()}">${ep.method}</span>
          <span class="endpoint-label">${ep.name}</span>
        </div>
      `;
      btn.onclick = () => selectEndpoint(ep);
      catGroup.appendChild(btn);
    });

    container.appendChild(catGroup);
  }
}

function selectEndpoint(endpoint) {
  currentEndpoint = endpoint;
  renderSidebar(document.getElementById('apiSearchInput')?.value || "");
  renderConsole();
}

function renderConsole() {
  const methodBadge = document.getElementById('endpointMethodBadge');
  if (methodBadge) {
    methodBadge.className = `badge-method ${currentEndpoint.method.toLowerCase()}`;
    methodBadge.textContent = currentEndpoint.method;
  }

  const pathText = document.getElementById('endpointPathText');
  if (pathText) pathText.textContent = currentEndpoint.path;

  const desc = document.getElementById('endpointDescription');
  if (desc) desc.textContent = currentEndpoint.description;

  // Path Parameters Section
  const pathParamsContainer = document.getElementById('pathParamsContainer');
  if (pathParamsContainer) {
    if (currentEndpoint.pathParams && currentEndpoint.pathParams.length > 0) {
      pathParamsContainer.style.display = 'flex';
      pathParamsContainer.innerHTML = `<div class="param-title">Path Parameters</div>`;
      currentEndpoint.pathParams.forEach(p => {
        const row = document.createElement('div');
        row.className = 'param-row';
        row.innerHTML = `
          <div class="param-label-group">
            <span class="param-name">${p.name}</span>
            <span class="param-type">${p.desc}</span>
          </div>
          <input type="text" id="pathParam_${p.name}" class="param-input" value="${p.default}" placeholder="${p.name}" />
        `;
        pathParamsContainer.appendChild(row);
        row.querySelector('input').addEventListener('input', updateCodePreview);
      });
    } else {
      pathParamsContainer.style.display = 'none';
      pathParamsContainer.innerHTML = "";
    }
  }

  // Query Parameters Section
  const queryParamsContainer = document.getElementById('queryParamsContainer');
  if (queryParamsContainer) {
    if (currentEndpoint.queryParams && currentEndpoint.queryParams.length > 0) {
      queryParamsContainer.style.display = 'flex';
      queryParamsContainer.innerHTML = `<div class="param-title">Query Parameters</div>`;
      currentEndpoint.queryParams.forEach(q => {
        const row = document.createElement('div');
        row.className = 'param-row';
        row.innerHTML = `
          <div class="param-label-group">
            <span class="param-name">${q.name}</span>
            <span class="param-type">${q.desc}</span>
          </div>
          <input type="text" id="queryParam_${q.name}" class="param-input" value="${q.default}" placeholder="${q.name}" />
        `;
        queryParamsContainer.appendChild(row);
        row.querySelector('input').addEventListener('input', updateCodePreview);
      });
    } else {
      queryParamsContainer.style.display = 'none';
      queryParamsContainer.innerHTML = "";
    }
  }

  // Request Body Section
  const bodyEditorWrapper = document.getElementById('bodyEditorWrapper');
  const bodyTextarea = document.getElementById('requestBodyTextarea');
  if (bodyEditorWrapper && bodyTextarea) {
    if (currentEndpoint.method !== 'GET' && currentEndpoint.defaultBody) {
      bodyEditorWrapper.style.display = 'flex';
      bodyTextarea.value = JSON.stringify(currentEndpoint.defaultBody, null, 2);
    } else {
      bodyEditorWrapper.style.display = 'none';
      bodyTextarea.value = '';
    }
  }

  // Auth token input sync
  const authInput = document.getElementById('authTokenInput');
  if (authInput) authInput.value = activeAuthToken;

  // Clear previous response view
  const statusElem = document.getElementById('responseStatusBar');
  if (statusElem) statusElem.innerHTML = `<span class="response-metric-pill">Ready to execute</span>`;

  const viewer = document.getElementById('responseViewer');
  if (viewer) viewer.innerHTML = `<div style="color: var(--text-muted); font-style: italic;">Click "Send Request" to trigger ${currentEnvironment === 'mock' ? 'Mock Sandbox' : 'Live Gateway'} execution.</div>`;

  updateCodePreview();
}

function gatherRequestValues() {
  const pathParams = {};
  if (currentEndpoint.pathParams) {
    currentEndpoint.pathParams.forEach(p => {
      const input = document.getElementById(`pathParam_${p.name}`);
      pathParams[p.name] = input ? input.value.trim() : p.default;
    });
  }

  const queryParams = {};
  if (currentEndpoint.queryParams) {
    currentEndpoint.queryParams.forEach(q => {
      const input = document.getElementById(`queryParam_${q.name}`);
      if (input && input.value.trim() !== "") {
        queryParams[q.name] = input.value.trim();
      }
    });
  }

  let body = null;
  const bodyTextarea = document.getElementById('requestBodyTextarea');
  if (currentEndpoint.method !== 'GET' && bodyTextarea && bodyTextarea.value.trim() !== '') {
    try {
      body = JSON.parse(bodyTextarea.value.trim());
    } catch (e) {
      body = { raw: bodyTextarea.value.trim() };
    }
  }

  const authInput = document.getElementById('authTokenInput');
  const token = authInput ? authInput.value.trim() : "";

  return { pathParams, queryParams, body, token };
}

function updateCodePreview() {
  const { pathParams, queryParams, body, token } = gatherRequestValues();
  const snippets = generateCodeSnippets(currentEndpoint, pathParams, queryParams, body, token);
  const snippetBox = document.getElementById('codeSnippetBox');
  if (snippetBox) {
    snippetBox.textContent = snippets[activeLangTab] || snippets.curl;
  }
}

// Execute Request Handler (Mock or Live)
async function executeRequest() {
  const sendBtn = document.getElementById('btnSendRequest');
  if (!sendBtn) return;
  const originalBtnText = sendBtn.innerHTML;
  sendBtn.innerHTML = `
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="spin"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
    <span>Executing...</span>
  `;
  sendBtn.disabled = true;

  const { pathParams, queryParams, body, token } = gatherRequestValues();
  const startTime = performance.now();

  try {
    if (currentEnvironment === "mock") {
      // Simulated realistic fintech network latency (120ms - 260ms)
      const simulatedLatency = Math.floor(Math.random() * 140) + 120;
      await new Promise(resolve => setTimeout(resolve, simulatedLatency));

      const mockResult = currentEndpoint.mockHandler({
        pathParams,
        queryParams,
        body,
        token
      });

      const duration = (performance.now() - startTime).toFixed(0);
      renderResponse(mockResult.status, mockResult.data, duration, "Mock Engine (Memory)");
    } else {
      // Live HTTP Request
      let effectivePath = currentEndpoint.path;
      currentEndpoint.pathParams.forEach(p => {
        effectivePath = effectivePath.replace(`{${p.name}}`, encodeURIComponent(pathParams[p.name] || p.default));
      });
      const queryString = Object.keys(queryParams).length > 0 ? "?" + new URLSearchParams(queryParams).toString() : "";
      const url = liveBaseUrl + effectivePath + queryString;

      const headers = {
        'Accept': 'application/json'
      };
      if (body) headers['Content-Type'] = 'application/json';
      if (token) {
        if (token.startsWith("pf_live_")) headers['X-API-Key'] = token;
        else headers['Authorization'] = `Bearer ${token}`;
      }

      const fetchOptions = {
        method: currentEndpoint.method,
        headers: headers
      };
      if (currentEndpoint.method !== 'GET' && body) {
        fetchOptions.body = JSON.stringify(body);
      }

      const res = await fetch(url, fetchOptions);
      const duration = (performance.now() - startTime).toFixed(0);
      let resJson;
      const text = await res.text();
      try {
        resJson = JSON.parse(text);
      } catch (e) {
        resJson = { responseText: text };
      }

      renderResponse(res.status, resJson, duration, "Live Gateway :8080");
    }
  } catch (err) {
    const duration = (performance.now() - startTime).toFixed(0);
    renderResponse(500, {
      success: false,
      error: {
        errorCode: "GATEWAY_CONNECTION_ERROR",
        message: err.message || "Failed to reach live backend gateway. Check if backend is running on port 8080 or switch to Mock Sandbox.",
        status: 500
      },
      traceId: generateTraceId()
    }, duration, "Error");
  } finally {
    sendBtn.innerHTML = originalBtnText;
    sendBtn.disabled = false;
  }
}

function renderResponse(status, data, durationMs, source) {
  const statusBar = document.getElementById('responseStatusBar');
  if (!statusBar) return;
  let statusClass = 's2xx';
  if (status >= 400 && status < 500) statusClass = 's4xx';
  if (status >= 500) statusClass = 's5xx';

  statusBar.innerHTML = `
    <span class="badge-status ${statusClass}">${status} ${getStatusText(status)}</span>
    <span class="response-metric-pill">⚡ ${durationMs} ms</span>
    <span class="response-metric-pill">📦 ${source}</span>
  `;

  const viewer = document.getElementById('responseViewer');
  if (viewer) {
    viewer.innerHTML = `<div class="response-json-display">${formatJsonWithHighlight(data)}</div>`;
  }
}

function getStatusText(status) {
  const map = {
    200: "OK",
    201: "Created",
    400: "Bad Request",
    401: "Unauthorized",
    403: "Forbidden",
    404: "Not Found",
    409: "Conflict",
    422: "Unprocessable Entity",
    500: "Internal Server Error"
  };
  return map[status] || "";
}

// ============================================================================
// 9. Sequence Flow & ADRs Rendering
// ============================================================================

function renderSequenceFlow(flowKey = 'saga') {
  const flow = FLOWS_CATALOG[flowKey] || FLOWS_CATALOG.saga;
  const titleDisplay = document.getElementById('flowTitleDisplay');
  if (titleDisplay) titleDisplay.textContent = flow.title;

  const listContainer = document.getElementById('sequenceStepsList');
  if (!listContainer) return;
  listContainer.innerHTML = "";

  flow.steps.forEach(s => {
    const item = document.createElement('div');
    item.className = 'sequence-step-item';
    item.innerHTML = `
      <div class="step-num-bubble">${s.num}</div>
      <div class="step-info-col">
        <div class="step-title-row">
          <h5>${s.title}</h5>
          <span class="step-service-tag">${s.service}</span>
        </div>
        <p class="step-desc">${s.desc}</p>
      </div>
    `;
    listContainer.appendChild(item);
  });
}

function renderADRs(filterCat = 'All') {
  const container = document.getElementById('adrsList');
  if (!container) return;
  container.innerHTML = "";

  const filtered = filterCat === 'All' ? ADR_CATALOG : ADR_CATALOG.filter(a => a.category.toLowerCase() === filterCat.toLowerCase());

  filtered.forEach(adr => {
    const card = document.createElement('div');
    card.className = 'adr-card';
    card.innerHTML = `
      <div class="adr-header">
        <div class="adr-title-group">
          <span class="adr-num-tag">${adr.id}</span>
          <h3>${adr.title}</h3>
        </div>
        <div class="adr-badges">
          <span class="badge-adr-status">${adr.status}</span>
          <span class="badge-adr-cat">${adr.category}</span>
        </div>
      </div>

      <div class="adr-section-block">
        <h4>1. Context & Problem Statement</h4>
        <p>${adr.context}</p>
      </div>

      <div class="adr-section-block">
        <h4>2. Evaluated Alternatives & Trade-off Matrix</h4>
        <table class="tradeoff-table">
          <thead>
            <tr>
              <th style="width: 25%;">Architecture Pattern</th>
              <th style="width: 40%;">Key Advantages (Pros)</th>
              <th style="width: 35%;">Engineering Costs / Drawbacks (Cons)</th>
            </tr>
          </thead>
          <tbody>
            ${adr.alternatives.map(alt => `
              <tr>
                <td style="font-weight: 600; color: var(--text-primary);">${alt.option}</td>
                <td style="color: #4ade80;">${alt.pros}</td>
                <td style="color: #f87171;">${alt.cons}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>

      <div class="adr-section-block">
        <h4>3. Decision Outcome & Rationale</h4>
        <p>${adr.decision}</p>
      </div>

      <div class="adr-section-block">
        <h4>4. Managed Trade-offs & Mitigations</h4>
        <p>${adr.tradeoffs}</p>
      </div>

      <div class="adr-section-block">
        <h4>5. Direct Code Implementation Reference</h4>
        <div>
          <span class="code-ref-pill">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
            ${adr.codeRef}
          </span>
        </div>
      </div>
    `;
    container.appendChild(card);
  });
}

// ============================================================================
// 10. Spec Download Handlers (Fallback for Fetch & Direct Data URL)
// ============================================================================

async function downloadFile(filename, relativeUrl) {
  try {
    const res = await fetch(relativeUrl);
    if (!res.ok) throw new Error("HTTP " + res.status);
    const blob = await res.blob();
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    setTimeout(() => {
      document.body.removeChild(link);
      URL.revokeObjectURL(link.href);
    }, 200);
    showToast('Downloaded ' + filename);
  } catch (err) {
    // If fetch failed (e.g. file:/// protocol), trigger direct window open/download link
    const link = document.createElement('a');
    link.href = relativeUrl;
    link.download = filename;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    setTimeout(() => {
      document.body.removeChild(link);
    }, 200);
    showToast('Initiated download of ' + filename);
  }
}

// ============================================================================
// 11. Initialization & Event Bindings
// ============================================================================

function initPortal() {
  // Navigation Tabs switching
  document.querySelectorAll('.nav-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.nav-tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
      btn.classList.add('active');
      const targetId = btn.getAttribute('data-tab');
      const targetElem = document.getElementById(targetId);
      if (targetElem) targetElem.classList.add('active');
    });
  });

  // Environment Selector
  const envSelect = document.getElementById('envSelector');
  if (envSelect) {
    envSelect.addEventListener('change', (e) => {
      currentEnvironment = e.target.value;
      const dot = document.getElementById('envStatusDot');
      if (dot) {
        if (currentEnvironment === 'mock') {
          dot.style.backgroundColor = 'var(--accent-emerald)';
          dot.style.boxShadow = '0 0 8px var(--accent-emerald)';
          showToast('Switched to Mock Sandbox Mode (100% Offline Simulation)');
        } else {
          dot.style.backgroundColor = 'var(--accent-cyan)';
          dot.style.boxShadow = '0 0 8px var(--accent-cyan)';
          showToast('Switched to Live Gateway Mode (Target: http://localhost:8080)');
        }
      }
      updateCodePreview();
    });
  }

  // Search input
  const searchInput = document.getElementById('apiSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      renderSidebar(e.target.value);
    });
  }

  // Quick Role Auth Tokens
  document.querySelectorAll('.btn-token-quick').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.btn-token-quick').forEach(b => b.classList.remove('active'));
      const role = btn.getAttribute('data-role');
      if (role === 'clear') {
        activeAuthToken = '';
      } else {
        btn.classList.add('active');
        activeAuthToken = PRESET_TOKENS[role] || '';
      }
      const authInput = document.getElementById('authTokenInput');
      if (authInput) authInput.value = activeAuthToken;
      updateCodePreview();
      showToast(`Applied ${btn.textContent.trim()} Credentials`);
    });
  });

  const authInput = document.getElementById('authTokenInput');
  if (authInput) {
    authInput.addEventListener('input', (e) => {
      activeAuthToken = e.target.value;
      updateCodePreview();
    });
  }

  // Code Lang Tabs
  document.querySelectorAll('.btn-lang-tab').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.btn-lang-tab').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeLangTab = btn.getAttribute('data-lang');
      updateCodePreview();
    });
  });

  // Copy Code Button
  const copyBtn = document.getElementById('btnCopyCode');
  if (copyBtn) {
    copyBtn.addEventListener('click', () => {
      const code = document.getElementById('codeSnippetBox')?.textContent || "";
      navigator.clipboard.writeText(code).then(() => {
        showToast('Code snippet copied to clipboard!');
      });
    });
  }

  // Send Request Button
  const sendReqBtn = document.getElementById('btnSendRequest');
  if (sendReqBtn) {
    sendReqBtn.addEventListener('click', executeRequest);
  }

  // Flow Selector Buttons
  document.querySelectorAll('.btn-flow-select').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.btn-flow-select').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderSequenceFlow(btn.getAttribute('data-flow'));
    });
  });

  // ADR Filter Buttons
  document.querySelectorAll('.btn-adr-filter').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.btn-adr-filter').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderADRs(btn.getAttribute('data-cat'));
    });
  });

  // Download Spec Buttons
  const dlOpenApiBtn = document.getElementById('btnDownloadOpenApi');
  if (dlOpenApiBtn) {
    dlOpenApiBtn.addEventListener('click', () => {
      downloadFile('openapi-spec.json', 'openapi-spec.json');
    });
  }

  const dlPostmanBtn = document.getElementById('btnDownloadPostman');
  if (dlPostmanBtn) {
    dlPostmanBtn.addEventListener('click', () => {
      downloadFile('postman-collection.json', 'postman-collection.json');
    });
  }

  // Initial Render
  renderSidebar();
  renderConsole();
  renderSequenceFlow('saga');
  renderADRs('All');
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initPortal);
} else {
  initPortal();
}
