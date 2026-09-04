import React from 'react';
import { RefreshCw, AlertTriangle } from 'lucide-react';
import { Dialog } from '../ui/Dialog';
import { Button } from '../ui/Button';

interface ResetDemoDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
  isLoading: boolean;
}

export const ResetDemoDialog: React.FC<ResetDemoDialogProps> = ({
  isOpen,
  onClose,
  onConfirm,
  isLoading,
}) => {
  return (
    <Dialog
      isOpen={isOpen}
      onClose={onClose}
      title="Reset Demo Environment?"
      description="This will restore all fictional accounts, initial wallet balances (₹24,750.00 for John Doe), double-entry ledger records, and transactions to their pristine seeded state."
    >
      <div className="space-y-4 pt-1">
        <div className="p-3 bg-amber-50 border border-amber-200 rounded-md flex items-start gap-2.5 text-xs text-amber-800">
          <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <p>
            Any temporary transfers or top-ups performed during this recruiter session will be reverted. Non-demo accounts and records remain completely untouched.
          </p>
        </div>

        <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-100">
          <Button
            variant="secondary"
            size="sm"
            onClick={onClose}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={onConfirm}
            loading={isLoading}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
            className="bg-slate-900 hover:bg-slate-800 text-white"
          >
            {isLoading ? 'Resetting State...' : 'Reset Demo Data'}
          </Button>
        </div>
      </div>
    </Dialog>
  );
};
