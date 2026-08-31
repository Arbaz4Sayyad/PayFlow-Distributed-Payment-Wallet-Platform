import { apiClient } from './client';
import { ApiResponse, Wallet, WalletBalanceResponse, CurrencyCode } from '../types';

export interface CreateWalletPayload {
  currency: CurrencyCode;
}

export interface WalletOperationPayload {
  amount: number;
  currency: CurrencyCode;
  referenceId?: string;
  description?: string;
}

export async function createWallet(payload: CreateWalletPayload): Promise<Wallet> {
  const response = await apiClient.post<ApiResponse<Wallet>>('/v1/wallets', payload);
  return response.data.data;
}

export async function getWallet(walletId: string): Promise<Wallet> {
  const response = await apiClient.get<ApiResponse<Wallet>>(`/v1/wallets/${walletId}`);
  return response.data.data;
}

export async function getWalletBalance(walletId: string): Promise<WalletBalanceResponse> {
  const response = await apiClient.get<ApiResponse<WalletBalanceResponse>>(`/v1/wallets/${walletId}/balance`);
  return response.data.data;
}

export async function topUpWallet(
  walletId: string,
  payload: WalletOperationPayload,
  idempotencyKey?: string
): Promise<WalletBalanceResponse> {
  const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
  const response = await apiClient.post<ApiResponse<WalletBalanceResponse>>(
    `/v1/wallets/${walletId}/top-up`,
    payload,
    { headers }
  );
  return response.data.data;
}

export async function withdrawWallet(
  walletId: string,
  payload: WalletOperationPayload,
  idempotencyKey?: string
): Promise<WalletBalanceResponse> {
  const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
  const response = await apiClient.post<ApiResponse<WalletBalanceResponse>>(
    `/v1/wallets/${walletId}/withdraw`,
    payload,
    { headers }
  );
  return response.data.data;
}
