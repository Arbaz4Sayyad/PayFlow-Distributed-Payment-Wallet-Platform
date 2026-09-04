export const ROUTES = {
  // Public Landing
  LANDING: '/',

  // Auth
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',

  // Customer App
  DASHBOARD: '/app/dashboard',
  WALLET: '/app/wallet',
  TRANSFERS: '/app/transfers',
  PAYMENTS: '/app/payments',
  TRANSACTIONS: '/app/transactions',
  TRANSACTION_DETAIL: (id: string = ':id') => `/app/transactions/${id}`,
  NOTIFICATIONS: '/app/notifications',
  PROFILE: '/app/profile',
  SECURITY: '/app/security',

  // Admin / Operations Portal
  ADMIN_DASHBOARD: '/admin',
  ADMIN_USERS: '/admin/users',
  ADMIN_WALLETS: '/admin/wallets',
  ADMIN_PAYMENTS: '/admin/payments',
  ADMIN_FRAUD: '/admin/fraud',
  ADMIN_EVENTS: '/admin/events',
} as const;
