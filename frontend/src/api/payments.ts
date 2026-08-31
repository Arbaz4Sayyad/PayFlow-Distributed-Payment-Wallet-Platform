import { apiClient } from './client';
import { ApiResponse, Transaction, CurrencyCode } from '../types';

export interface InitiatePaymentPayload {
  merchantId: string;
  amount: number;
  currency: CurrencyCode;
  orderId?: string;
  idempotencyKey: string;
}

export interface RefundPaymentPayload {
  paymentId: string;
  reason?: string;
  amount?: number;
  idempotencyKey: string;
}

export async function initiateMerchantPayment(
  payload: InitiatePaymentPayload
): Promise<Transaction> {
  const response = await apiClient.post<ApiResponse<Transaction>>(
    '/v1/payments',
    {
      merchantId: payload.merchantId,
      amount: payload.amount,
      currency: payload.currency,
      orderId: payload.orderId,
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey,
      },
    }
  );
  return response.data.data;
}

export async function requestRefund(
  payload: RefundPaymentPayload
): Promise<Transaction> {
  const response = await apiClient.post<ApiResponse<Transaction>>(
    `/v1/refunds/${payload.paymentId}`,
    {
      reason: payload.reason,
      amount: payload.amount,
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey,
      },
    }
  );
  return response.data.data;
}
