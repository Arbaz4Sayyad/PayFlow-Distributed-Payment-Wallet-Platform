import { apiClient, TOKEN_STORAGE_KEY } from './client';
import { ApiResponse, AuthResponse, Transaction } from '../types';
import { MOCK_TRANSACTIONS } from '../mocks/mockData';

export const DEMO_STORAGE_BALANCE_KEY = 'payflow_demo_wallet_balance';
export const DEMO_STORAGE_TRANSACTIONS_KEY = 'payflow_demo_transactions';

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
    name: 'Arbaz Sayyad',
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
 * Gets current demo balance from localStorage or initializes it
 */
export function getDemoBalance(): number {
  try {
    const raw = localStorage.getItem(DEMO_STORAGE_BALANCE_KEY);
    if (raw !== null) {
      const parsed = parseFloat(raw);
      if (!isNaN(parsed)) return parsed;
    }
  } catch {
    // ignore
  }
  return DEMO_CONFIG.primaryUser.initialBalance;
}

/**
 * Sets and persists demo balance in localStorage
 */
export function setDemoBalance(newBalance: number): number {
  try {
    const safeBal = Math.max(0, parseFloat(newBalance.toFixed(2)));
    localStorage.setItem(DEMO_STORAGE_BALANCE_KEY, safeBal.toString());
    return safeBal;
  } catch {
    return newBalance;
  }
}

/**
 * Gets demo transactions from localStorage or initializes from MOCK_TRANSACTIONS
 */
export function getDemoTransactions(): Transaction[] {
  try {
    const raw = localStorage.getItem(DEMO_STORAGE_TRANSACTIONS_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed;
      }
    }
  } catch {
    // ignore
  }
  return MOCK_TRANSACTIONS;
}

/**
 * Appends a newly executed transaction to the top of the demo ledger in localStorage
 */
export function addDemoTransaction(
  txn: Omit<Transaction, 'id' | 'createdAt'> & { id?: string; createdAt?: string }
): Transaction {
  const fullTxn: Transaction = {
    id: txn.id || `txn_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
    transactionNumber: txn.transactionNumber || `TXN-${Math.floor(10000 + Math.random() * 90000)}`,
    senderWalletId: txn.senderWalletId,
    recipientWalletId: txn.recipientWalletId,
    senderName: txn.senderName,
    recipientName: txn.recipientName,
    amount: txn.amount,
    amountMinor: txn.amountMinor || Math.round(txn.amount * 100),
    currency: txn.currency || 'INR',
    type: txn.type,
    status: txn.status || 'COMPLETED',
    description: txn.description,
    referenceId: txn.referenceId || `REF-${Date.now().toString().slice(-6)}`,
    createdAt: txn.createdAt || new Date().toISOString(),
    completedAt: txn.completedAt || new Date().toISOString(),
  };

  try {
    const existing = getDemoTransactions();
    const updated = [fullTxn, ...existing];
    localStorage.setItem(DEMO_STORAGE_TRANSACTIONS_KEY, JSON.stringify(updated));
  } catch {
    // ignore
  }

  return fullTxn;
}

/**
 * Resets local demo storage back to defaults
 */
export function resetDemoStore(): void {
  try {
    localStorage.removeItem(DEMO_STORAGE_BALANCE_KEY);
    localStorage.removeItem(DEMO_STORAGE_TRANSACTIONS_KEY);
  } catch {
    // ignore
  }
}

/**
 * Performs 1-click recruiter demo authentication using the backend /api/v1/auth/login endpoint
 * with immediate resilient fallback so the demo user experience never fails.
 */
export async function demoLogin(): Promise<AuthResponse> {
  const fallbackAuth: AuthResponse = {
    accessToken: `demo_session_${Date.now()}`,
    refreshToken: `demo_refresh_${Date.now()}`,
    tokenType: 'Bearer',
    expiresInSeconds: 86400,
    user: {
      id: DEMO_CONFIG.primaryUser.walletId,
      email: DEMO_CONFIG.primaryUser.email,
      firstName: 'Arbaz',
      lastName: 'Sayyad',
      phone: DEMO_CONFIG.primaryUser.phone,
      role: 'ROLE_ADMIN',
      status: 'ACTIVE',
      kycLevel: 'TIER_3',
      createdAt: new Date().toISOString(),
    },
  };

  try {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      '/v1/auth/login',
      {
        email: DEMO_CONFIG.primaryUser.email,
        password: DEMO_CONFIG.primaryUser.password,
      },
      { timeout: 5000 }
    );

    const authData = response.data?.data;
    if (authData?.accessToken) {
      localStorage.setItem(TOKEN_STORAGE_KEY, authData.accessToken);
      return authData;
    }
    localStorage.setItem(TOKEN_STORAGE_KEY, fallbackAuth.accessToken);
    return fallbackAuth;
  } catch (err: any) {
    // If backend reports user missing (401/404), attempt registration
    if (err.response?.status === 401 || err.response?.status === 404) {
      try {
        const regResponse = await apiClient.post<ApiResponse<AuthResponse>>(
          '/v1/auth/register',
          {
            email: DEMO_CONFIG.primaryUser.email,
            password: DEMO_CONFIG.primaryUser.password,
            phone: DEMO_CONFIG.primaryUser.phone,
            firstName: 'Arbaz',
            lastName: 'Sayyad',
            role: 'ROLE_USER',
          },
          { timeout: 5000 }
        );
        const authData = regResponse.data?.data;
        if (authData?.accessToken) {
          localStorage.setItem(TOKEN_STORAGE_KEY, authData.accessToken);
          return authData;
        }
      } catch {
        // Fall through to fallback session
      }
    }

    // Backend unreachable, 502/503/504 Bad Gateway, cold starting or network error:
    // Seamlessly activate verified demo user session
    localStorage.setItem(TOKEN_STORAGE_KEY, fallbackAuth.accessToken);
    return fallbackAuth;
  }
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
  resetDemoStore();
  try {
    const response = await apiClient.post<ApiResponse<{ message: string }>>('/v1/demo/reset');
    return { success: true, message: response.data?.data?.message || 'Demo data restored successfully' };
  } catch {
    return { success: true, message: 'Demo environment reset initiated' };
  }
}
