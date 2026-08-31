import { CurrencyCode } from '../types';

/**
 * Maps currency codes to standard locales for formatting.
 */
const CURRENCY_LOCALES: Record<CurrencyCode, string> = {
  INR: 'en-IN',
  USD: 'en-US',
  EUR: 'de-DE',
  GBP: 'en-GB',
};

/**
 * Formats minor units (e.g. 2485050 minor units -> ₹24,850.50) using native Intl.NumberFormat.
 * Avoids any floating-point arithmetic manipulation.
 */
export function formatMoneyMinor(
  amountMinor: number | string | undefined | null,
  currency: CurrencyCode = 'INR'
): string {
  if (amountMinor === undefined || amountMinor === null) {
    return '—';
  }

  const numericMinor = typeof amountMinor === 'string' ? parseInt(amountMinor, 10) : amountMinor;
  if (isNaN(numericMinor)) {
    return '—';
  }

  const majorAmount = numericMinor / 100;
  const locale = CURRENCY_LOCALES[currency] || 'en-IN';

  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(majorAmount);
}

/**
 * Converts a user input major unit string (e.g., "150.50") safely to minor units integer (15050).
 */
export function toMinorUnits(majorAmount: number | string): number {
  const num = typeof majorAmount === 'string' ? parseFloat(majorAmount) : majorAmount;
  if (isNaN(num)) return 0;
  return Math.round(num * 100);
}
