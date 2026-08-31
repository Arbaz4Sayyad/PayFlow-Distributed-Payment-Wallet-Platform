import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  Activity,
  Users,
  Wallet,
  Layers,
  ShieldAlert,
  Terminal,
  ArrowLeft,
} from 'lucide-react';
import { ROUTES } from '../../constants/routes';

const ADMIN_NAV = [
  { label: 'System Health & Metrics', path: ROUTES.ADMIN_DASHBOARD, icon: <Activity className="w-3.5 h-3.5" /> },
  { label: 'User Accounts', path: ROUTES.ADMIN_USERS, icon: <Users className="w-3.5 h-3.5" /> },
  { label: 'Wallet Master Ledger', path: ROUTES.ADMIN_WALLETS, icon: <Wallet className="w-3.5 h-3.5" /> },
  { label: 'Payment Sagas', path: ROUTES.ADMIN_PAYMENTS, icon: <Layers className="w-3.5 h-3.5" /> },
  { label: 'Fraud Review Queue', path: ROUTES.ADMIN_FRAUD, icon: <ShieldAlert className="w-3.5 h-3.5" /> },
  { label: 'Outbox / DLT Events', path: ROUTES.ADMIN_EVENTS, icon: <Terminal className="w-3.5 h-3.5" /> },
];

export const AdminLayout: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      {/* Top Operations Header */}
      <header className="bg-slate-950 border-b border-slate-800 px-6 h-12 flex items-center justify-between shrink-0 select-none">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(ROUTES.DASHBOARD)}
            className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-white px-2 py-1 rounded bg-slate-800/60 hover:bg-slate-800 transition-colors"
          >
            <ArrowLeft className="w-3 h-3" />
            <span>Customer View</span>
          </button>
          <div className="h-3 w-px bg-slate-800" />
          <span className="text-xs font-bold tracking-wider text-slate-200 uppercase">
            PayFlow
          </span>
          <span className="text-[10px] font-mono uppercase bg-amber-500/10 text-amber-400 border border-amber-500/20 px-1.5 py-0.5 rounded">
            Operations Portal
          </span>
        </div>

        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-[11px] font-mono text-emerald-400">All Microservices Healthy</span>
        </div>
      </header>

      {/* Admin Secondary Bar Nav */}
      <nav className="bg-slate-900/90 border-b border-slate-800 px-6 flex items-center gap-1 overflow-x-auto select-none">
        {ADMIN_NAV.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === ROUTES.ADMIN_DASHBOARD}
            className={({ isActive }) =>
              `flex items-center gap-2 px-3 py-2.5 text-xs font-medium border-b-2 transition-colors whitespace-nowrap ${
                isActive
                  ? 'border-amber-400 text-amber-300 font-semibold'
                  : 'border-transparent text-slate-400 hover:text-slate-200 hover:border-slate-700'
              }`
            }
          >
            {item.icon}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Main Operational Canvas */}
      <main className="flex-1 p-6 max-w-7xl w-full mx-auto overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
};
