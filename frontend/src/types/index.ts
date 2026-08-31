// Currency & Financial Types
export type CurrencyCode = 'INR' | 'USD' | 'EUR' | 'GBP';

// Status Types
export type TransactionStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUND_PENDING'
  | 'REFUNDED'
  | 'REVERSED';

export type WalletStatus = 'ACTIVE' | 'FROZEN' | 'CLOSED';
export type UserRole = 'ROLE_USER' | 'ROLE_MERCHANT' | 'ROLE_ADMIN' | 'ROLE_SUPPORT';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'BLOCKED' | 'PENDING_KYC';
export type FraudRiskScore = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

// User & Auth Types
export interface UserProfile {
  id: string;
  email: string;
  phone: string;
  firstName?: string;
  lastName?: string;
  role: UserRole;
  status: UserStatus;
  kycLevel: 'TIER_1' | 'TIER_2' | 'TIER_3';
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresInSeconds?: number;
  user: UserProfile;
}

// Wallet Types
export interface Wallet {
  id: string;
  userId: string;
  currency: CurrencyCode;
  balance: number;
  balanceMinor: number;
  status: WalletStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface WalletBalanceResponse {
  walletId: string;
  currency: CurrencyCode;
  balance: number;
  balanceMinor: number;
  formattedBalance: string;
}

// Transaction & Payment Types
export type TransactionType = 'TRANSFER' | 'TOPUP' | 'WITHDRAW' | 'MERCHANT_PAYMENT' | 'REFUND';

export interface Transaction {
  id: string;
  transactionNumber: string;
  senderWalletId: string;
  recipientWalletId?: string;
  senderName?: string;
  recipientName?: string;
  amount: number;
  amountMinor: number;
  currency: CurrencyCode;
  type: TransactionType;
  status: TransactionStatus;
  description?: string;
  referenceId?: string;
  createdAt: string;
  completedAt?: string;
  metadata?: Record<string, unknown>;
}

export interface PaymentSagaLifecycleStep {
  step: string;
  label: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED' | 'SKIPPED';
  timestamp?: string;
  details?: string;
}

// Notification Types
export interface NotificationItem {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: 'PAYMENT' | 'SECURITY' | 'WALLET' | 'SYSTEM';
  isRead: boolean;
  createdAt: string;
}

// Admin / Operations Types
export interface SystemHealthMetrics {
  paymentsTodayCount: number;
  paymentsTodayVolumeMinor: number;
  successRatePercentage: number;
  failedRatePercentage: number;
  fraudReviewPendingCount: number;
  activeUsersCount: number;
  totalWalletsCount: number;
}

export interface FraudReviewItem {
  id: string;
  transactionId: string;
  userEmail: string;
  amountMinor: number;
  currency: CurrencyCode;
  riskScore: FraudRiskScore;
  triggeredRules: string[];
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface OutboxEventLog {
  id: string;
  aggregateType: string;
  aggregateId: string;
  eventType: string;
  payload: string;
  status: 'PUBLISHED' | 'FAILED' | 'DLT';
  retryCount: number;
  createdAt: string;
}

// Common Generic API Envelope
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    status: number;
    path?: string;
    validationErrors?: Array<{ field: string; message: string }>;
  };
  timestamp: string;
  traceId?: string;
}
