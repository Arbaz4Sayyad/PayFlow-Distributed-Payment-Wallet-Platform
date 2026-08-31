import React from 'react';
import { CurrencyCode } from '../../types';
import { formatMoneyMinor } from '../../utils/currency';

export interface MoneyAmountProps {
  amountMinor: number | string | undefined | null;
  currency?: CurrencyCode;
  type?: 'credit' | 'debit' | 'neutral';
  showSign?: boolean;
  size?: 'sm' | 'md' | 'lg' | 'kpi';
  className?: string;
}

export const MoneyAmount: React.FC<MoneyAmountProps> = ({
  amountMinor,
  currency = 'INR',
  type = 'neutral',
  showSign = false,
  size = 'md',
  className = '',
}) => {
  const formatted = formatMoneyMinor(amountMinor, currency);

  const sizeClasses = {
    sm: 'text-xs',
    md: 'text-sm font-medium',
    lg: 'text-base font-semibold',
    kpi: 'text-2xl sm:text-3xl font-bold tracking-tight',
  };

  const typeClasses = {
    credit: 'text-emerald-600',
    debit: 'text-slate-900',
    neutral: 'text-slate-900',
  };

  let prefix = '';
  if (showSign && amountMinor !== undefined && amountMinor !== null) {
    if (type === 'credit') prefix = '+';
    if (type === 'debit') prefix = '-';
  }

  return (
    <span
      className={`tabular-nums inline-flex items-baseline ${sizeClasses[size]} ${typeClasses[type]} ${className}`}
    >
      {prefix && <span className="mr-0.5">{prefix}</span>}
      {formatted}
    </span>
  );
};
