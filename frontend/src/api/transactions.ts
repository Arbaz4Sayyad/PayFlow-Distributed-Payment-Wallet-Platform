import { apiClient } from './client';
import { ApiResponse, Transaction } from '../types';

export interface TransactionFilterParams {
  page?: number;
  size?: number;
  type?: string;
  status?: string;
  search?: string;
  startDate?: string;
  endDate?: string;
}

export interface PaginatedTransactions {
  items: Transaction[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export async function getTransactions(
  params?: TransactionFilterParams
): Promise<PaginatedTransactions> {
  const response = await apiClient.get<ApiResponse<PaginatedTransactions>>('/v1/transactions', {
    params,
  });
  return response.data.data;
}

export async function getTransaction(id: string): Promise<Transaction> {
  const response = await apiClient.get<ApiResponse<Transaction>>(`/v1/transactions/${id}`);
  return response.data.data;
}
