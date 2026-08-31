import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Wallet,
  ArrowUpRight,
  Store,
  Receipt,
  Bell,
  ShieldCheck,
  User,
  LogOut,
  ShieldAlert,
} from 'lucide-react';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';

interface NavItem {
  label: string;
  path: string;
  icon: React.ReactNode;
}

const PRIMARY_NAV: NavItem[] = [
  { label: 'Overview', path: ROUTES.DASHBOARD, icon: <LayoutDashboard className="w-4 h-4" /> },
  { label: 'Wallet & Balances', path: ROUTES.WALLET, icon: <Wallet className="w-4 h-4" /> },
  { label: 'Send Money', path: ROUTES.TRANSFERS, icon: <ArrowUpRight className="w-4 h-4" /> },
  { label: 'Merchant Payments', path: ROUTES.PAYMENTS, icon: <Store className="w-4 h-4" /> },
  { label: 'Transaction Ledger', path: ROUTES.TRANSACTIONS, icon: <Receipt className="w-4 h-4" /> },
];

const SECONDARY_NAV: NavItem[] = [
  { label: 'Notifications', path: ROUTES.NOTIFICATIONS, icon: <Bell className="w-4 h-4" /> },
  { label: 'Security & Keys', path: ROUTES.SECURITY, icon: <ShieldCheck className="w-4 h-4" /> },
  { label: 'Account Settings', path: ROUTES.PROFILE, icon: <User className="w-4 h-4" /> },
];

export const Sidebar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ROLE_ADMIN';

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  return (
    <aside className="hidden md:flex flex-col w-56 h-screen bg-white border-r border-slate-200 shrink-0 select-none">
      {/* Brand Header */}
      <div className="flex items-center gap-2.5 px-5 h-14 border-b border-slate-100">
        <img src="/favicon.svg" alt="PayFlow" className="w-6 h-6 rounded" />
        <span className="font-semibold text-sm tracking-tight text-slate-900">PayFlow</span>
      </div>

      {/* Main Navigation */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        <div>
          <p className="px-2 pb-1.5 text-[11px] font-semibold tracking-wider uppercase text-slate-600">
            Platform
          </p>
          <nav className="space-y-0.5">
            {PRIMARY_NAV.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-100 text-slate-900 font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`
                }
              >
                <span className="text-slate-600">{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        <div>
          <p className="px-2 pb-1.5 text-[11px] font-semibold tracking-wider uppercase text-slate-600">
            Account
          </p>
          <nav className="space-y-0.5">
            {SECONDARY_NAV.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-slate-100 text-slate-900 font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`
                }
              >
                <span className="text-slate-600">{item.icon}</span>
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Admin Link if admin user */}
        {isAdmin && (
          <div>
            <p className="px-2 pb-1.5 text-[11px] font-semibold tracking-wider uppercase text-slate-600">
              Operations
            </p>
            <NavLink
              to={ROUTES.ADMIN_DASHBOARD}
              className="flex items-center gap-2.5 px-2.5 py-1.5 rounded-md text-xs font-medium text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200/60 transition-colors"
            >
              <ShieldAlert className="w-4 h-4 text-amber-700" />
              <span>Operations Portal</span>
            </NavLink>
          </div>
        )}
      </div>

      {/* User Footer */}
      <div className="p-3 border-t border-slate-100 bg-slate-50/50">
        <div className="flex items-center justify-between px-2 py-1.5">
          <div className="min-w-0 flex-1 mr-2">
            <p className="text-xs font-medium text-slate-900 truncate">
              {user?.firstName ? `${user.firstName} ${user.lastName || ''}` : user?.email || 'Authenticated User'}
            </p>
            <p className="text-[11px] text-slate-600 truncate">{user?.email}</p>
          </div>
          <button
            onClick={handleLogout}
            title="Sign out"
            className="p-1 text-slate-600 hover:text-red-600 hover:bg-white rounded transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </aside>
  );
};
