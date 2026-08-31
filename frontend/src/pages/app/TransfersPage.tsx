import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ArrowUpRight,
  ShieldCheck,
  User,
  Clock,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Dialog } from '../../components/ui/Dialog';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { useToast } from '../../components/ui/Toast';
import { useIdempotency } from '../../hooks/useIdempotency';
import { ROUTES } from '../../constants/routes';
import { MOCK_WALLET } from '../../mocks/mockData';
import { toMinorUnits } from '../../utils/currency';

interface InitiatedResult {
  transactionId: string;
  transactionNumber: string;
  status: string;
  recipient: string;
  amount: number;
}

export const TransfersPage: React.FC = () => {
  const navigate = useNavigate();
  const { success, error: toastError } = useToast();
  const { getIdempotencyKey, resetIdempotencyKey } = useIdempotency();

  // Transfer Form State
  const [recipient, setRecipient] = useState('rahul.sharma@payflow.com');
  const [amount, setAmount] = useState('1500.00');
  const [note, setNote] = useState('Project consulting settlement');

  // Confirmation Modal
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Result state
  const [result, setResult] = useState<InitiatedResult | null>(null);

  const numericAmount = parseFloat(amount) || 0;
  const amountMinor = toMinorUnits(numericAmount);

  const handleOpenReview = (e: React.FormEvent) => {
    e.preventDefault();

    if (!recipient.trim()) {
      toastError('Recipient Required', 'Please enter a valid email or wallet address.');
      return;
    }

    if (numericAmount <= 0) {
      toastError('Invalid Amount', 'Please enter a valid transfer amount.');
      return;
    }

    if (amountMinor > MOCK_WALLET.balanceMinor) {
      toastError('Insufficient Funds', 'Transfer amount exceeds your current available balance.');
      return;
    }

    setIsReviewOpen(true);
  };

  const handleConfirmTransfer = async () => {
    setIsSubmitting(true);
    getIdempotencyKey();

    // Simulating backend Saga initiation and double-entry posting
    setTimeout(() => {
      setIsSubmitting(false);
      setIsReviewOpen(false);
      resetIdempotencyKey();

      const txnNum = 'TXN-' + Math.floor(10000 + Math.random() * 90000);
      const initiated: InitiatedResult = {
        transactionId: 'txn_' + Math.random().toString(36).substring(2, 9),
        transactionNumber: txnNum,
        status: 'PROCESSING',
        recipient,
        amount: numericAmount,
      };

      setResult(initiated);
      success('Transfer Initiated', `Transfer ${txnNum} has been queued for execution.`);
    }, 750);
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      {/* Header */}
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Send Money</h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Transfer funds instantly to any verified PayFlow wallet, merchant, or email.
        </p>
      </div>

      {result ? (
        /* Transfer Status Screen */
        <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle space-y-6 animate-in fade-in">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-600">
              <Clock className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-slate-900">Transfer Initiated</h2>
              <p className="text-xs text-slate-500">
                Saga Orchestrator is executing double-entry ledger settlement.
              </p>
            </div>
          </div>

          <div className="bg-slate-50 border border-slate-200 rounded-md p-4 space-y-3 text-xs">
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Transaction ID:</span>
              <span className="font-mono font-semibold text-slate-900">{result.transactionNumber}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Recipient:</span>
              <span className="font-medium text-slate-900">{result.recipient}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Amount:</span>
              <span className="font-bold text-slate-900">
                <MoneyAmount amountMinor={toMinorUnits(result.amount)} currency="INR" size="md" />
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Status:</span>
              <StatusIndicator status={result.status} size="sm" />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setResult(null)}
            >
              Send Another Transfer
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={() => navigate(ROUTES.TRANSACTIONS)}
            >
              View in Ledger
            </Button>
          </div>
        </div>
      ) : (
        /* Transfer Input Form */
        <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle">
          <form onSubmit={handleOpenReview} className="space-y-4">
            <Input
              label="Recipient"
              type="text"
              required
              value={recipient}
              onChange={(e) => setRecipient(e.target.value)}
              placeholder="rahul.sharma@payflow.com or Wallet UUID"
              prefix={<User className="w-4 h-4 text-slate-400" />}
              description="Enter the recipient's verified PayFlow email or 36-character wallet ID"
            />

            <div className="space-y-1">
              <div className="flex justify-between items-center">
                <label className="block text-xs font-medium text-slate-700">Amount</label>
                <span className="text-[11px] text-slate-500">
                  Available: <span className="font-semibold text-slate-800">₹24,850.50</span>
                </span>
              </div>
              <Input
                type="number"
                step="0.01"
                min="1"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                prefix={<span className="text-slate-500 font-medium">₹</span>}
                suffix={<span className="text-xs font-semibold text-slate-400">INR</span>}
              />
            </div>

            <Input
              label="Payment Reference / Note (Optional)"
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. Invoice settlement, Rent, Dinner"
            />

            <div className="pt-2">
              <Button
                type="submit"
                variant="primary"
                className="w-full"
                rightIcon={<ArrowUpRight className="w-4 h-4" />}
              >
                Continue to Review
              </Button>
            </div>
          </form>
        </div>
      )}

      {/* Idempotent Confirmation Dialog */}
      <Dialog
        isOpen={isReviewOpen}
        onClose={() => !isSubmitting && setIsReviewOpen(false)}
        title="Review Transfer"
        description="Verify transfer parameters before executing irrevocable ledger transaction."
        footer={
          <>
            <Button
              variant="secondary"
              size="sm"
              disabled={isSubmitting}
              onClick={() => setIsReviewOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={isSubmitting}
              disabled={isSubmitting}
              onClick={handleConfirmTransfer}
            >
              Confirm Transfer
            </Button>
          </>
        }
      >
        <div className="space-y-3 text-xs">
          <div className="p-3 bg-slate-50 border border-slate-200 rounded-md space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-slate-500">From Source:</span>
              <span className="font-medium text-slate-900">Primary Wallet (•••• 4F82)</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500">To Recipient:</span>
              <span className="font-semibold text-slate-900">{recipient}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-500">Transfer Note:</span>
              <span className="text-slate-700 italic">{note || 'None'}</span>
            </div>
            <div className="border-t border-slate-200 pt-2 flex justify-between items-center">
              <span className="text-slate-600 font-medium">Transfer Amount:</span>
              <span className="font-mono text-slate-900">₹{numericAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-600 font-medium">Platform Fee:</span>
              <span className="font-mono text-emerald-600 font-semibold">₹0.00</span>
            </div>
            <div className="border-t border-slate-200 pt-2 flex justify-between items-center text-sm font-bold text-slate-900">
              <span>Total Debit:</span>
              <span>₹{numericAmount.toFixed(2)} INR</span>
            </div>
          </div>

          <div className="flex items-center gap-2 text-[11px] text-slate-500">
            <ShieldCheck className="w-4 h-4 text-slate-600 shrink-0" />
            <span>Idempotency-Key armed. Double clicks are safely guarded against re-execution.</span>
          </div>
        </div>
      </Dialog>
    </div>
  );
};
