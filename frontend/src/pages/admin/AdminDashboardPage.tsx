import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, Terminal } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { ROUTES } from '../../constants/routes';
import { MOCK_ADMIN_METRICS, MOCK_FRAUD_QUEUE, MOCK_OUTBOX_EVENTS } from '../../mocks/mockData';

export const AdminDashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const metrics = MOCK_ADMIN_METRICS;

  return (
    <div className="space-y-6">
      {/* Top operational metrics */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <div className="bg-slate-950 border border-slate-800 rounded-md p-4 space-y-1">
          <span className="text-[11px] font-mono uppercase text-slate-400">Total Volume Today</span>
          <div className="text-xl font-bold font-mono text-white">
            <MoneyAmount amountMinor={metrics.paymentsTodayVolumeMinor} currency="INR" size="lg" className="text-white" />
          </div>
          <span className="text-[11px] text-slate-500">{metrics.paymentsTodayCount.toLocaleString()} Payments</span>
        </div>

        <div className="bg-slate-950 border border-slate-800 rounded-md p-4 space-y-1">
          <span className="text-[11px] font-mono uppercase text-slate-400">Success Rate</span>
          <div className="text-xl font-bold font-mono text-emerald-400">
            {metrics.successRatePercentage}%
          </div>
          <span className="text-[11px] text-slate-500">Saga Settled</span>
        </div>

        <div className="bg-slate-950 border border-slate-800 rounded-md p-4 space-y-1">
          <span className="text-[11px] font-mono uppercase text-slate-400">Failure Rate</span>
          <div className="text-xl font-bold font-mono text-red-400">
            {metrics.failedRatePercentage}%
          </div>
          <span className="text-[11px] text-slate-500">Compensating txns</span>
        </div>

        <div className="bg-slate-950 border border-slate-800 rounded-md p-4 space-y-1">
          <span className="text-[11px] font-mono uppercase text-slate-400">Fraud Queue Pending</span>
          <div className="text-xl font-bold font-mono text-amber-400">
            {metrics.fraudReviewPendingCount}
          </div>
          <span className="text-[11px] text-slate-500">Manual review</span>
        </div>
      </div>

      {/* Fraud Queue & Outbox Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Urgent Fraud Review Queue */}
        <div className="bg-slate-950 border border-slate-800 rounded-md p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 text-amber-400" />
              <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-200">
                Fraud Review Queue
              </h2>
            </div>
            <Button
              variant="ghost"
              size="sm"
              className="text-amber-400 hover:text-amber-300"
              onClick={() => navigate(ROUTES.ADMIN_FRAUD)}
            >
              Open Full Queue →
            </Button>
          </div>

          <div className="space-y-2">
            {MOCK_FRAUD_QUEUE.map((item) => (
              <div
                key={item.id}
                className="p-3 bg-slate-900 border border-slate-800 rounded text-xs space-y-1.5"
              >
                <div className="flex justify-between items-center">
                  <span className="font-mono text-amber-400 font-bold">{item.transactionId}</span>
                  <span className="text-[10px] font-mono bg-red-950 text-red-400 border border-red-800 px-1.5 py-0.5 rounded">
                    {item.riskScore} RISK
                  </span>
                </div>
                <p className="text-slate-300">{item.reason}</p>
                <div className="flex justify-between text-[11px] text-slate-500 pt-1">
                  <span>{item.userEmail}</span>
                  <MoneyAmount amountMinor={item.amountMinor} currency={item.currency} size="sm" className="text-slate-300" />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Live Outbox Events Feed */}
        <div className="bg-slate-950 border border-slate-800 rounded-md p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Terminal className="w-4 h-4 text-emerald-400" />
              <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-200">
                Transactional Outbox & Events
              </h2>
            </div>
            <Button
              variant="ghost"
              size="sm"
              className="text-emerald-400 hover:text-emerald-300"
              onClick={() => navigate(ROUTES.ADMIN_EVENTS)}
            >
              Stream →
            </Button>
          </div>

          <div className="space-y-2 font-mono text-xs">
            {MOCK_OUTBOX_EVENTS.map((evt) => (
              <div
                key={evt.id}
                className="p-2.5 bg-slate-900 border border-slate-800 rounded flex items-center justify-between"
              >
                <div>
                  <span className="text-emerald-400 font-bold">{evt.eventType}</span>
                  <span className="text-slate-500 text-[11px] block">{evt.aggregateId}</span>
                </div>
                <span className="text-[10px] bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded">
                  {evt.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
