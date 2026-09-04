import React, { useState } from 'react';
import { Check, ShieldCheck, AlertCircle } from 'lucide-react';
import { MoneyAmount } from '../../components/ui/MoneyAmount';

type Scenario = 'transfer' | 'topup' | 'overdraft';

export const HeroInteractiveWidget: React.FC = () => {
  const [scenario, setScenario] = useState<Scenario>('transfer');

  return (
    <div className="bg-white border border-slate-200 rounded-xl shadow-card overflow-hidden text-left">
      {/* Widget Header */}
      <div className="bg-slate-50/80 border-b border-slate-200/80 px-4 py-3 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-500" />
          <span className="text-xs font-semibold text-slate-800">
            Live Payment Simulation
          </span>
        </div>
        <div className="flex items-center gap-1 bg-white border border-slate-200/80 p-0.5 rounded-md text-[11px]">
          <button
            onClick={() => setScenario('transfer')}
            className={`px-2.5 py-1 rounded font-medium transition-colors cursor-pointer ${
              scenario === 'transfer' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Send ₹1,200
          </button>
          <button
            onClick={() => setScenario('topup')}
            className={`px-2.5 py-1 rounded font-medium transition-colors cursor-pointer ${
              scenario === 'topup' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Top up ₹5,000
          </button>
          <button
            onClick={() => setScenario('overdraft')}
            className={`px-2.5 py-1 rounded font-medium transition-colors cursor-pointer ${
              scenario === 'overdraft' ? 'bg-slate-900 text-white shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Overdraft Check
          </button>
        </div>
      </div>

      {/* Widget Body */}
      <div className="p-5 space-y-4 bg-slate-50/30">
        {/* Scenario 1: P2P Transfer */}
        {scenario === 'transfer' && (
          <div className="space-y-3 animate-in fade-in duration-150">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="bg-white border border-slate-200 rounded-lg p-3.5 space-y-1">
                <div className="flex items-center justify-between text-xs text-slate-500">
                  <span>Alice (Sender)</span>
                  <span className="font-mono text-[11px]">••••4821</span>
                </div>
                <div className="flex items-baseline gap-2">
                  <MoneyAmount amountMinor={380000} currency="INR" size="sm" />
                  <span className="text-xs font-semibold text-rose-600 font-mono">-₹1,200.00</span>
                </div>
                <div className="text-[11px] text-slate-400">Previous balance: ₹5,000.00</div>
              </div>

              <div className="bg-white border border-slate-200 rounded-lg p-3.5 space-y-1">
                <div className="flex items-center justify-between text-xs text-slate-500">
                  <span>Bob (Recipient)</span>
                  <span className="font-mono text-[11px]">••••9104</span>
                </div>
                <div className="flex items-baseline gap-2">
                  <MoneyAmount amountMinor={220000} currency="INR" size="sm" />
                  <span className="text-xs font-semibold text-emerald-600 font-mono">+₹1,200.00</span>
                </div>
                <div className="text-[11px] text-slate-400">Previous balance: ₹1,000.00</div>
              </div>
            </div>

            <div className="bg-white border border-slate-200 rounded-lg p-3 space-y-2 text-xs">
              <div className="flex items-center justify-between border-b border-slate-100 pb-1.5">
                <span className="font-semibold text-slate-900 text-xs">Ledger Reconciliation</span>
                <span className="text-[10px] font-mono text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-200 font-medium">
                  Debit == Credit
                </span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-[11px]">
                <div className="p-2 bg-slate-50 rounded border border-slate-100">
                  <div className="text-slate-400 text-[10px]">DEBIT ENTRY</div>
                  <div className="font-semibold text-slate-800">₹1,200.00</div>
                  <div className="text-[10px] text-slate-500">Alice's Wallet</div>
                </div>
                <div className="p-2 bg-slate-50 rounded border border-slate-100">
                  <div className="text-slate-400 text-[10px]">CREDIT ENTRY</div>
                  <div className="font-semibold text-slate-800">₹1,200.00</div>
                  <div className="text-[10px] text-slate-500">Bob's Wallet</div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Scenario 2: Top Up */}
        {scenario === 'topup' && (
          <div className="space-y-3 animate-in fade-in duration-150">
            <div className="bg-white border border-slate-200 rounded-lg p-4 space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-600 font-medium">Bank Deposit Credited</span>
                <span className="font-mono text-xs text-emerald-600 font-semibold">+₹5,000.00</span>
              </div>
              <div className="flex items-baseline gap-2">
                <MoneyAmount amountMinor={2985050} currency="INR" size="kpi" />
                <span className="text-xs text-slate-400 font-semibold">INR</span>
              </div>
              <p className="text-xs text-slate-500">
                Funds are immediately available to send or withdraw.
              </p>
            </div>
          </div>
        )}

        {/* Scenario 3: Overdraft check */}
        {scenario === 'overdraft' && (
          <div className="space-y-3 animate-in fade-in duration-150">
            <div className="bg-white border border-rose-200 rounded-lg p-4 space-y-2">
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-800 font-medium">Attempting ₹99,000 transfer</span>
                <span className="text-[10px] font-mono text-rose-700 bg-rose-50 px-1.5 py-0.5 rounded border border-rose-200 font-medium">
                  Transfer Blocked
                </span>
              </div>
              <div className="p-2.5 bg-rose-50/60 rounded border border-rose-100 text-xs text-rose-800 flex items-start gap-2">
                <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                <div className="text-xs space-y-0.5">
                  <p className="font-semibold">Insufficient Balance</p>
                  <p className="text-rose-700 text-[11px]">
                    PayFlow automatically rejects transfers that exceed your available funds before any database change occurs.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Footer Meta */}
        <div className="flex items-center justify-between text-[11px] text-slate-500 pt-1 border-t border-slate-200/60">
          <div className="flex items-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-slate-600" />
            <span>Idempotency protected</span>
          </div>
          <div className="flex items-center gap-1 text-emerald-700 font-medium">
            <Check className="w-3.5 h-3.5" />
            <span>Settled</span>
          </div>
        </div>
      </div>
    </div>
  );
};
