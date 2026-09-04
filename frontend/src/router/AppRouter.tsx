import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { ROUTES } from '../constants/routes';
import { useAuth } from '../hooks/useAuth';
import { AppLayout } from '../components/layout/AppLayout';
import { AdminLayout } from '../components/layout/AdminLayout';

// Landing Page
import { LandingPage } from '../pages/landing/LandingPage';

// Auth Pages
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterPage } from '../pages/auth/RegisterPage';

// Customer App Pages
import { DashboardPage } from '../pages/app/DashboardPage';
import { WalletPage } from '../pages/app/WalletPage';
import { TransfersPage } from '../pages/app/TransfersPage';
import { PaymentsPage } from '../pages/app/PaymentsPage';
import { TransactionsPage } from '../pages/app/TransactionsPage';
import { TransactionDetailPage } from '../pages/app/TransactionDetailPage';
import { NotificationsPage } from '../pages/app/NotificationsPage';
import { ProfilePage } from '../pages/app/ProfilePage';
import { SecurityPage } from '../pages/app/SecurityPage';

// Admin Operations Pages
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage';
import { AdminUsersPage } from '../pages/admin/AdminUsersPage';
import { AdminWalletsPage } from '../pages/admin/AdminWalletsPage';
import { AdminPaymentsPage } from '../pages/admin/AdminPaymentsPage';
import { AdminFraudPage } from '../pages/admin/AdminFraudPage';
import { AdminEventsPage } from '../pages/admin/AdminEventsPage';

// Guard: Require Authenticated Session
const RequireAuth: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="w-5 h-5 border-2 border-slate-900 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return <>{children}</>;
};

// Guard: Require Admin Role
const RequireAdmin: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="w-5 h-5 border-2 border-amber-400 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  if (user?.role !== 'ROLE_ADMIN') {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  return <>{children}</>;
};

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      {/* Public Landing Page */}
      <Route path="/" element={<LandingPage />} />

      {/* Auth Public Routes */}
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

      {/* Customer Application Shell */}
      <Route
        path="/app"
        element={
          <RequireAuth>
            <AppLayout />
          </RequireAuth>
        }
      >
        <Route index element={<Navigate to={ROUTES.DASHBOARD} replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="transfers" element={<TransfersPage />} />
        <Route path="payments" element={<PaymentsPage />} />
        <Route path="transactions" element={<TransactionsPage />} />
        <Route path="transactions/:id" element={<TransactionDetailPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="security" element={<SecurityPage />} />
      </Route>

      {/* Admin Operations Portal */}
      <Route
        path="/admin"
        element={
          <RequireAdmin>
            <AdminLayout />
          </RequireAdmin>
        }
      >
        <Route index element={<AdminDashboardPage />} />
        <Route path="users" element={<AdminUsersPage />} />
        <Route path="wallets" element={<AdminWalletsPage />} />
        <Route path="payments" element={<AdminPaymentsPage />} />
        <Route path="fraud" element={<AdminFraudPage />} />
        <Route path="events" element={<AdminEventsPage />} />
      </Route>

      {/* 404 Fallback */}
      <Route path="*" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
    </Routes>
  );
};
