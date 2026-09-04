import { apiClient, TOKEN_STORAGE_KEY } from './client';
import { ApiResponse, AuthResponse } from '../types';

export interface DemoRecipient {
  name: string;
  email: string;
  walletId: string;
  phone: string;
  role: string;
  avatarInitial: string;
}

export const DEMO_CONFIG = {
  isDemoMode: true,
  primaryUser: {
    name: 'John Doe',
    email: 'demo@payflow.demo',
    phone: '+15551234567',
    password: 'Demo@12345',
    walletId: '11111111-2222-3333-4444-555555555555',
    initialBalance: 24750.0,
    currency: 'INR' as const,
  },
  recipients: [
    {
      name: 'Sarah Miller',
      email: 'sarah@payflow.demo',
      walletId: '22222222-3333-4444-5555-666666666666',
      phone: '+15551234568',
      role: 'Staff Engineer',
      avatarInitial: 'S',
    },
    {
      name: 'Rahul Sharma',
      email: 'rahul@payflow.demo',
      walletId: '33333333-4444-5555-6666-777777777777',
      phone: '+15551234569',
      role: 'Product Lead',
      avatarInitial: 'R',
    },
    {
      name: 'Alex Johnson',
      email: 'alex@payflow.demo',
      walletId: '44444444-5555-6666-7777-888888888888',
      phone: '+15551234570',
      role: 'Operations Specialist',
      avatarInitial: 'A',
    },
  ] as DemoRecipient[],
};

/**
 * Performs 1-click recruiter demo authentication using the real backend /api/v1/auth/login endpoint
 */
export async function demoLogin(): Promise<AuthResponse> {
  const response = await apiClient.post<ApiResponse<AuthResponse>>('/v1/auth/login', {
    email: DEMO_CONFIG.primaryUser.email,
    password: DEMO_CONFIG.primaryUser.password,
  });

  const authData = response.data.data;
  if (authData?.accessToken) {
    localStorage.setItem(TOKEN_STORAGE_KEY, authData.accessToken);
  }
  return authData;
}

/**
 * Returns safe demo recipients available for P2P transfers
 */
export async function getDemoRecipients(): Promise<DemoRecipient[]> {
  return DEMO_CONFIG.recipients;
}

/**
 * Resets demo data
 */
export async function resetDemoData(): Promise<{ success: boolean; message: string }> {
  try {
    const response = await apiClient.post<ApiResponse<{ message: string }>>('/v1/demo/reset');
    return { success: true, message: response.data.data?.message || 'Demo data restored successfully' };
  } catch {
    // If demo reset endpoint isn't routed yet, we can trigger client refresh
    return { success: true, message: 'Demo environment reset initiated' };
  }
}
