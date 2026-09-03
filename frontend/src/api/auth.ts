import { apiClient } from './client';
import { ApiResponse, AuthResponse, UserProfile } from '../types';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  phone: string;
  firstName?: string;
  lastName?: string;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/login', payload);
  return response.data.data;
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/register', {
    email: payload.email,
    password: payload.password,
    phone: payload.phone,
    firstName: payload.firstName || 'User',
    lastName: payload.lastName || 'Demo',
    role: 'ROLE_USER',
  });
  return response.data.data;
}

export async function getCurrentUser(): Promise<UserProfile> {
  const response = await apiClient.get<ApiResponse<UserProfile>>('/v1/users/me');
  return response.data.data;
}

export async function getUserById(userId: string): Promise<UserProfile> {
  const response = await apiClient.get<ApiResponse<UserProfile>>(`/v1/users/${userId}`);
  return response.data.data;
}
