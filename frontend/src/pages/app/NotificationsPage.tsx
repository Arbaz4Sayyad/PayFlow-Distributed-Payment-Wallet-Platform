import React, { useState } from 'react';
import { CheckCheck, ShieldAlert, Wallet, CheckCircle2 } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../components/ui/Toast';
import { MOCK_NOTIFICATIONS } from '../../mocks/mockData';
import { formatRelativeTime } from '../../utils/dates';

export const NotificationsPage: React.FC = () => {
  const { success } = useToast();
  const [notifications, setNotifications] = useState(MOCK_NOTIFICATIONS);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD'>('ALL');

  const handleMarkAllRead = () => {
    setNotifications(notifications.map((n) => ({ ...n, isRead: true })));
    success('All Read', 'Marked all notifications as read.');
  };

  const filtered = notifications.filter((n) => (filter === 'UNREAD' ? !n.isRead : true));

  return (
    <div className="space-y-4 max-w-2xl mx-auto">
      <div className="flex items-center justify-between border-b border-slate-200 pb-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Notifications</h1>
          <p className="text-xs text-slate-500 mt-0.5">Real-time payment and security alerts.</p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          onClick={handleMarkAllRead}
          leftIcon={<CheckCheck className="w-3.5 h-3.5" />}
        >
          Mark all as read
        </Button>
      </div>

      <div className="flex gap-2 text-xs">
        <button
          onClick={() => setFilter('ALL')}
          className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
            filter === 'ALL' ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 border border-slate-200'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setFilter('UNREAD')}
          className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
            filter === 'UNREAD' ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 border border-slate-200'
          }`}
        >
          Unread only
        </button>
      </div>

      <div className="space-y-2">
        {filtered.map((item) => (
          <div
            key={item.id}
            className={`p-4 rounded-lg border transition-colors flex items-start gap-3 bg-white shadow-subtle ${
              item.isRead ? 'border-slate-200 opacity-80' : 'border-blue-200 bg-blue-50/20'
            }`}
          >
            <div className="mt-0.5 p-1.5 bg-slate-100 rounded-md text-slate-700">
              {item.type === 'PAYMENT' && <CheckCircle2 className="w-4 h-4 text-emerald-600" />}
              {item.type === 'WALLET' && <Wallet className="w-4 h-4 text-blue-600" />}
              {item.type === 'SECURITY' && <ShieldAlert className="w-4 h-4 text-amber-600" />}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between">
                <h4 className="text-xs font-semibold text-slate-900">{item.title}</h4>
                <span className="text-[11px] text-slate-400">{formatRelativeTime(item.createdAt)}</span>
              </div>
              <p className="text-xs text-slate-600 mt-0.5">{item.message}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
