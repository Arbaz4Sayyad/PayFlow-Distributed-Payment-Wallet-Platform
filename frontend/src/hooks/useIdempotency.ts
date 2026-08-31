import { useRef, useCallback } from 'react';

/**
 * Generates a stable UUIDv4 key for a financial action.
 * Preserves the key during retries/in-flight states and rotates only when explicitly reset.
 */
export function useIdempotency() {
  const keyRef = useRef<string | null>(null);

  const getOrGenerateKey = useCallback(() => {
    if (!keyRef.current) {
      keyRef.current = 'idemp_' + crypto.randomUUID();
    }
    return keyRef.current;
  }, []);

  const resetKey = useCallback(() => {
    keyRef.current = null;
  }, []);

  return {
    getIdempotencyKey: getOrGenerateKey,
    resetIdempotencyKey: resetKey,
  };
}
