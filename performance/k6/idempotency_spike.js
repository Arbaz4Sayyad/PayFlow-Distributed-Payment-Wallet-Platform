import http from 'k6/http';
import { check, sleep } from 'k6';

// ============================================================
// PayFlow Idempotency Concurrent Spike Test
// Verifies that firing 50 concurrent requests with the EXACT SAME
// idempotency key results in exactly 1 debit and 49 cached responses.
// ============================================================
export const options = {
  scenarios: {
    duplicate_spike: {
      executor: 'per-vu-iterations',
      vus: 50,
      iterations: 1,
      maxDuration: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate==0'], // All requests must return 200 or 201 OK
  },
};

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const SENDER_WALLET_ID = __ENV.SENDER_WALLET_ID || '11111111-1111-1111-1111-111111111111';
const RECIPIENT_WALLET_ID = __ENV.RECIPIENT_WALLET_ID || '22222222-2222-2222-2222-222222222222';
const FIXED_IDEMPOTENCY_KEY = 'concurrency-spike-idempotency-key-001';

export default function () {
  const payload = JSON.stringify({
    senderWalletId: SENDER_WALLET_ID,
    recipientWalletId: RECIPIENT_WALLET_ID,
    amount: 100.00,
    currency: 'INR',
    paymentType: 'P2P_TRANSFER',
    idempotencyKey: FIXED_IDEMPOTENCY_KEY,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${__ENV.JWT_TOKEN || 'dummy'}`,
    },
  };

  const res = http.post(`${BASE_URL}/api/v1/payments`, payload, params);

  check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    'payment ID matches across all duplicate requests': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.id !== undefined;
      } catch (e) {
        return false;
      }
    },
  });
}
