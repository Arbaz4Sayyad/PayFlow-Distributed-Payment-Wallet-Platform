import { apiClient } from './client';
import { ApiResponse, Transaction, CurrencyCode } from '../types';

export interface InitiateTransferPayload {
  recipientWalletId?: string;
  recipientEmail?: string;
  amount: number;
  currency: CurrencyCode;
  note?: string;
  idempotencyKey: string;
}

export async function initiateTransfer(
  payload: InitiateTransferPayload
): Promise<Transaction> {
  const response = await apiClient.post<ApiResponse<Transaction>>(
    '/v1/transfers',
    {
      recipientWalletId: payload.recipientWalletId,
      recipientEmail: payload.recipientEmail,
      amount: payload.amount,
      currency: payload.currency,
      description: payload.note,
    },
    {
      headers: {
        'Idempotency-Key': payload.idempotencyKey,
      },
    }
  );
  return response.data.data;
}
