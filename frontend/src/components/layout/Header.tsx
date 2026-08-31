import React from 'react';
import { Search, Bell, ChevronDown } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';

export const Header: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <header className="h-14 bg-white border-b border-slate-200 px-4 sm:px-6 flex items-center justify-between shrink-0 select-none">
      {/* Search trigger */}
      <div className="flex items-center gap-2">
        <div className="relative">
          <button
            onClick={() => navigate(ROUTES.TRANSACTIONS)}
            className="flex items-center gap-2.5 px-2.5 py-1.5 text-xs text-slate-500 bg-slate-50 border border-slate-200 rounded-md hover:bg-slate-100 hover:border-slate-300 transition-colors w-44 sm:w-64 text-left shadow-subtle"
          >
            <Search className="w-3.5 h-3.5 text-slate-400" />
            <span>Search ledger & payments...</span>
            <kbd className="hidden sm:inline-block ml-auto text-[10px] font-mono bg-white border border-slate-200 px-1.5 py-0.5 rounded text-slate-500">
              ⌘K
            </kbd>
          </button>
        </div>
      </div>

      {/* Right controls */}
      <div className="flex items-center gap-3">
        {/* Notification button */}
        <button
          onClick={() => navigate(ROUTES.NOTIFICATIONS)}
          className="relative p-1.5 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-md transition-colors"
          title="Notifications"
        >
          <Bell className="w-4 h-4" />
          <span className="absolute top-1 right-1 w-2 h-2 bg-blue-600 rounded-full ring-2 ring-white" />
        </button>

        <div className="h-4 w-px bg-slate-200 mx-0.5" />

        {/* User Pill */}
        <button
          onClick={() => navigate(ROUTES.PROFILE)}
          className="flex items-center gap-2 p-1 hover:bg-slate-50 rounded-md transition-colors"
        >
          <div className="w-7 h-7 rounded-full bg-slate-900 text-white flex items-center justify-center text-xs font-semibold">
            {user?.firstName ? user.firstName.charAt(0) : user?.email?.charAt(0).toUpperCase() || 'U'}
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-medium text-slate-900 leading-tight">
              {user?.firstName ? `${user.firstName} ${user.lastName || ''}` : user?.email?.split('@')[0]}
            </p>
            <p className="text-[10px] text-slate-600 leading-tight">
              {user?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Verified Customer'}
            </p>
          </div>
          <ChevronDown className="w-3 h-3 text-slate-400 hidden sm:block" />
        </button>
      </div>
    </header>
  );
};
