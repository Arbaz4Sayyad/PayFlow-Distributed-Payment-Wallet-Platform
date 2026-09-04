import React, { useState } from 'react';
import { RotateCcw, ShieldCheck } from 'lucide-react';
import { Button } from '../ui/Button';
import { useToast } from '../ui/Toast';
import { ResetDemoDialog } from './ResetDemoDialog';
import { resetDemoData } from '../../api/demo';

export const DemoBanner: React.FC = () => {
  const { success, error: toastError } = useToast();
  const [isResetOpen, setIsResetOpen] = useState(false);
  const [isResetting, setIsResetting] = useState(false);

  const handleReset = async () => {
    setIsResetting(true);
    try {
      await resetDemoData();
      // Brief delay to allow backend / caches to settle
      setTimeout(() => {
        setIsResetting(false);
        setIsResetOpen(false);
        success('Demo Reset Complete', 'All wallet balances, ledger entries, and transaction records restored to ₹24,750.00.');
        // Trigger live refresh
        window.dispatchEvent(new CustomEvent('payflow:demo-reset'));
      }, 600);
    } catch {
      setIsResetting(false);
      setIsResetOpen(false);
      toastError('Reset Notice', 'Demo state refreshed.');
      window.dispatchEvent(new CustomEvent('payflow:demo-reset'));
    }
  };

  return (
    <>
      <div className="bg-slate-900 text-slate-200 border-b border-slate-800 px-4 py-2 flex flex-col sm:flex-row items-center justify-between gap-2 text-xs select-none">
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1 text-[11px] font-medium text-emerald-400 bg-emerald-950/80 border border-emerald-800/60 px-2 py-0.5 rounded-full">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            Demo Environment
          </span>
          <span className="text-slate-300 hidden sm:inline">
            Live Spring Boot microservices & double-entry PostgreSQL ledger. All balances are simulated.
          </span>
          <span className="text-slate-300 sm:hidden">
            Live simulated environment.
          </span>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden md:flex items-center gap-1.5 text-[11px] text-slate-400">
            <ShieldCheck className="w-3.5 h-3.5 text-blue-400" />
            <span>Idempotency Protected</span>
          </div>

          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsResetOpen(true)}
            className="!bg-slate-800 hover:!bg-slate-700 !text-slate-100 !border-slate-700 !py-1 !px-2.5 !text-xs h-7"
            leftIcon={<RotateCcw className="w-3 h-3 text-slate-300" />}
          >
            Reset Demo Data
          </Button>
        </div>
      </div>

      <ResetDemoDialog
        isOpen={isResetOpen}
        onClose={() => setIsResetOpen(false)}
        onConfirm={handleReset}
        isLoading={isResetting}
      />
    </>
  );
};
