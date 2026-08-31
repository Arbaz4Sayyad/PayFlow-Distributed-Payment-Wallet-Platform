import { apiClient } from './client';
import {
  ApiResponse,
  SystemHealthMetrics,
  FraudReviewItem,
  OutboxEventLog,
  UserProfile,
  Wallet,
  Transaction,
} from '../types';

export async function getAdminMetrics(): Promise<SystemHealthMetrics> {
  const response = await apiClient.get<ApiResponse<SystemHealthMetrics>>('/v1/admin/metrics');
  return response.data.data;
}

export async function getAdminUsers(): Promise<UserProfile[]> {
  const response = await apiClient.get<ApiResponse<UserProfile[]>>('/v1/admin/users');
  return response.data.data;
}

export async function getAdminWallets(): Promise<Wallet[]> {
  const response = await apiClient.get<ApiResponse<Wallet[]>>('/v1/admin/wallets');
  return response.data.data;
}

export async function getAdminPayments(): Promise<Transaction[]> {
  const response = await apiClient.get<ApiResponse<Transaction[]>>('/v1/admin/payments');
  return response.data.data;
}

export async function getFraudReviewQueue(): Promise<FraudReviewItem[]> {
  const response = await apiClient.get<ApiResponse<FraudReviewItem[]>>('/v1/fraud/queue');
  return response.data.data;
}

export async function reviewFraudItem(
  id: string,
  action: 'APPROVE' | 'REJECT'
): Promise<void> {
  await apiClient.post(`/v1/fraud/queue/${id}/review`, { action });
}

export async function getOutboxEventLogs(): Promise<OutboxEventLog[]> {
  const response = await apiClient.get<ApiResponse<OutboxEventLog[]>>('/v1/admin/events');
  return response.data.data;
}
