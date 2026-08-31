import React from 'react';
import { TransactionStatus, WalletStatus } from '../../types';

export type DisplayStatus =
  | TransactionStatus
  | WalletStatus
  | 'ACTIVE'
  | 'FROZEN'
  | 'SUSPENDED'
  | 'BLOCKED'
  | 'REVIEW'
  | 'PENDING_KYC';

interface StatusConfig {
  dotColor: string;
  textColor: string;
  bgColor: string;
  borderColor: string;
  label: string;
}

const STATUS_CONFIGS: Record<string, StatusConfig> = {
  COMPLETED: {
    dotColor: 'bg-emerald-500',
    textColor: 'text-emerald-800',
    bgColor: 'bg-emerald-50',
    borderColor: 'border-emerald-200',
    label: 'Completed',
  },
  ACTIVE: {
    dotColor: 'bg-emerald-500',
    textColor: 'text-emerald-800',
    bgColor: 'bg-emerald-50',
    borderColor: 'border-emerald-200',
    label: 'Active',
  },
  PROCESSING: {
    dotColor: 'bg-amber-500 animate-pulse',
    textColor: 'text-amber-800',
    bgColor: 'bg-amber-50',
    borderColor: 'border-amber-200',
    label: 'Processing',
  },
  PENDING: {
    dotColor: 'bg-amber-500',
    textColor: 'text-amber-800',
    bgColor: 'bg-amber-50',
    borderColor: 'border-amber-200',
    label: 'Pending',
  },
  PENDING_KYC: {
    dotColor: 'bg-amber-500',
    textColor: 'text-amber-800',
    bgColor: 'bg-amber-50',
    borderColor: 'border-amber-200',
    label: 'KYC Pending',
  },
  REVIEW: {
    dotColor: 'bg-amber-500',
    textColor: 'text-amber-800',
    bgColor: 'bg-amber-50',
    borderColor: 'border-amber-200',
    label: 'In Review',
  },
  FAILED: {
    dotColor: 'bg-red-500',
    textColor: 'text-red-800',
    bgColor: 'bg-red-50',
    borderColor: 'border-red-200',
    label: 'Failed',
  },
  CANCELLED: {
    dotColor: 'bg-slate-400',
    textColor: 'text-slate-700',
    bgColor: 'bg-slate-50',
    borderColor: 'border-slate-200',
    label: 'Cancelled',
  },
  FROZEN: {
    dotColor: 'bg-blue-500',
    textColor: 'text-blue-800',
    bgColor: 'bg-blue-50',
    borderColor: 'border-blue-200',
    label: 'Frozen',
  },
  REFUND_PENDING: {
    dotColor: 'bg-amber-500',
    textColor: 'text-amber-800',
    bgColor: 'bg-amber-50',
    borderColor: 'border-amber-200',
    label: 'Refund Pending',
  },
  REFUNDED: {
    dotColor: 'bg-slate-500',
    textColor: 'text-slate-800',
    bgColor: 'bg-slate-100',
    borderColor: 'border-slate-200',
    label: 'Refunded',
  },
  SUSPENDED: {
    dotColor: 'bg-red-500',
    textColor: 'text-red-800',
    bgColor: 'bg-red-50',
    borderColor: 'border-red-200',
    label: 'Suspended',
  },
  BLOCKED: {
    dotColor: 'bg-red-500',
    textColor: 'text-red-800',
    bgColor: 'bg-red-50',
    borderColor: 'border-red-200',
    label: 'Blocked',
  },
};

export interface StatusIndicatorProps {
  status: DisplayStatus | string;
  size?: 'sm' | 'md';
  className?: string;
}

export const StatusIndicator: React.FC<StatusIndicatorProps> = ({
  status,
  size = 'md',
  className = '',
}) => {
  const normalizedKey = status.toUpperCase();
  const config = STATUS_CONFIGS[normalizedKey] || {
    dotColor: 'bg-slate-400',
    textColor: 'text-slate-700',
    bgColor: 'bg-slate-50',
    borderColor: 'border-slate-200',
    label: status,
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 border rounded-sm ${config.bgColor} ${config.borderColor} ${config.textColor} ${
        size === 'sm' ? 'px-1.5 py-0.5 text-[11px]' : 'px-2 py-0.5 text-xs'
      } font-medium select-none ${className}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${config.dotColor}`} />
      <span>{config.label}</span>
    </span>
  );
};
