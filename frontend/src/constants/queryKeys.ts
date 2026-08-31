export const QUERY_KEYS = {
  auth: {
    me: ['auth', 'me'] as const,
  },
  wallet: {
    all: ['wallet'] as const,
    primary: ['wallet', 'primary'] as const,
    balance: (id: string) => ['wallet', id, 'balance'] as const,
  },
  transactions: {
    all: ['transactions'] as const,
    list: (filters?: Record<string, unknown>) => ['transactions', 'list', filters] as const,
    detail: (id: string) => ['transactions', 'detail', id] as const,
  },
  notifications: {
    all: ['notifications'] as const,
    unreadCount: ['notifications', 'unreadCount'] as const,
  },
  admin: {
    metrics: ['admin', 'metrics'] as const,
    users: (params?: Record<string, unknown>) => ['admin', 'users', params] as const,
    wallets: (params?: Record<string, unknown>) => ['admin', 'wallets', params] as const,
    payments: (params?: Record<string, unknown>) => ['admin', 'payments', params] as const,
    fraudQueue: ['admin', 'fraudQueue'] as const,
    eventLogs: (params?: Record<string, unknown>) => ['admin', 'eventLogs', params] as const,
  },
} as const;
