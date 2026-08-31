import { useMemo } from 'react';
import { CurrencyCode } from '../types';
import { formatMoneyMinor, toMinorUnits } from '../utils/currency';

export function useMoney(amountMinor?: number | null, currency: CurrencyCode = 'INR') {
  const formatted = useMemo(() => {
    return formatMoneyMinor(amountMinor, currency);
  }, [amountMinor, currency]);

  return {
    formatted,
    toMinorUnits,
  };
}
