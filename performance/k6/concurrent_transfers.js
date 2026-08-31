import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// ============================================================
// PayFlow 1,000 TPS Concurrent Payment Stress Test
// ============================================================
export const options = {
  stages: [
    { duration: '30s', target: 100 },  // Ramp up to 100 VUs
    { duration: '1m',  target: 500 },  // Ramp up to 500 VUs
    { duration: '2m',  target: 1000 }, // Sustain 1,000 VUs peak load
    { duration: '30s', target: 0 },    // Ramp down to 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% < 500ms, 99% < 1000ms
    http_req_failed: ['rate<0.01'],                 // Error rate < 1%
  },
};

const BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const SENDER_WALLET_ID = __ENV.SENDER_WALLET_ID || '11111111-1111-1111-1111-111111111111';
const RECIPIENT_WALLET_ID = __ENV.RECIPIENT_WALLET_ID || '22222222-2222-2222-2222-222222222222';
const JWT_TOKEN = __ENV.JWT_TOKEN || 'dummy_token';

export default function () {
  const idempotencyKey = `k6-tx-${uuidv4()}`;

  const payload = JSON.stringify({
    senderWalletId: SENDER_WALLET_ID,
    recipientWalletId: RECIPIENT_WALLET_ID,
    amount: 10.50,
    currency: 'INR',
    paymentType: 'P2P_TRANSFER',
    idempotencyKey: idempotencyKey,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${JWT_TOKEN}`,
      'X-Trace-Id': `trace-${uuidv4().substring(0, 16)}`,
    },
  };

  const res = http.post(`${BASE_URL}/api/v1/payments`, payload, params);

  check(res, {
    'status is 201 or 200': (r) => r.status === 201 || r.status === 200,
    'response has success flag': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true;
      } catch (e) {
        return false;
      }
    },
    'latency is under 1s': (r) => r.timings.duration < 1000,
  });

  sleep(0.1);
}
