import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Wallet, ArrowUpRight, Receipt, MoreHorizontal } from 'lucide-react';
import { ROUTES } from '../../constants/routes';

export const MobileNav: React.FC = () => {
  return (
    <div className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-200 px-3 py-1.5 flex items-center justify-around shadow-lg">
      <NavLink
        to={ROUTES.DASHBOARD}
        className={({ isActive }) =>
          `flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-slate-900 font-semibold' : 'text-slate-500 hover:text-slate-900'
          }`
        }
      >
        <LayoutDashboard className="w-4 h-4" />
        <span>Overview</span>
      </NavLink>

      <NavLink
        to={ROUTES.WALLET}
        className={({ isActive }) =>
          `flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-slate-900 font-semibold' : 'text-slate-500 hover:text-slate-900'
          }`
        }
      >
        <Wallet className="w-4 h-4" />
        <span>Wallet</span>
      </NavLink>

      <NavLink
        to={ROUTES.TRANSFERS}
        className={({ isActive }) =>
          `flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-slate-900 font-semibold' : 'text-slate-500 hover:text-slate-900'
          }`
        }
      >
        <ArrowUpRight className="w-4 h-4" />
        <span>Send</span>
      </NavLink>

      <NavLink
        to={ROUTES.TRANSACTIONS}
        className={({ isActive }) =>
          `flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-slate-900 font-semibold' : 'text-slate-500 hover:text-slate-900'
          }`
        }
      >
        <Receipt className="w-4 h-4" />
        <span>Ledger</span>
      </NavLink>

      <NavLink
        to={ROUTES.PROFILE}
        className={({ isActive }) =>
          `flex flex-col items-center gap-1 py-1 px-2 text-[10px] font-medium transition-colors ${
            isActive ? 'text-slate-900 font-semibold' : 'text-slate-500 hover:text-slate-900'
          }`
        }
      >
        <MoreHorizontal className="w-4 h-4" />
        <span>More</span>
      </NavLink>
    </div>
  );
};
