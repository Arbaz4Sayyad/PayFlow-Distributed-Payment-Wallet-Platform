import React, { useState, useEffect, useCallback } from 'react';
import {
  Plus,
  ArrowDownLeft,
  RefreshCw,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Dialog } from '../../components/ui/Dialog';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { useToast } from '../../components/ui/Toast';
import { useIdempotency } from '../../hooks/useIdempotency';
import { getWalletBalance, topUpWallet, withdrawWallet } from '../../api/wallet';
import { apiClient } from '../../api/client';
import { DEMO_CONFIG } from '../../api/demo';
import { formatDateTime } from '../../utils/dates';
import { toMinorUnits } from '../../utils/currency';
import { Transaction } from '../../types';

export const WalletPage: React.FC = () => {
  const { success, error: toastError } = useToast();
  const { getIdempotencyKey, resetIdempotencyKey } = useIdempotency();

  const walletId = DEMO_CONFIG.primaryUser.walletId;
  const [balance, setBalance] = useState<number>(DEMO_CONFIG.primaryUser.initialBalance);
  const [currency, setCurrency] = useState<string>('INR');
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Modals
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form states
  const [addAmount, setAddAmount] = useState('');
  const [addMethod, setAddMethod] = useState('BANK_TRANSFER');
  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [withdrawBank, setWithdrawBank] = useState('HDFC Bank •••• 4832');

  const fetchLiveWallet = useCallback(async () => {
    try {
      const balRes = await getWalletBalance(walletId);
      if (balRes) {
        setBalance(balRes.balance);
        setCurrency(balRes.currency || 'INR');
      }
    } catch {
      // fallback
    }

    try {
      const ledgerRes = await apiClient.get(`/v1/ledger/wallets/${walletId}`);
      if (ledgerRes.data?.data?.content) {
        const postings = ledgerRes.data.data.content;
        const txns: Transaction[] = postings.map((line: any, idx: number) => ({
          id: line.id || `line-${idx}`,
          transactionNumber: `TXN-${String(10000 + idx).padStart(5, '0')}`,
          senderWalletId: line.entryType === 'DEBIT' ? walletId : 'EXT-CLEARING',
          recipientWalletId: line.entryType === 'CREDIT' ? walletId : 'EXT-VENDOR',
          senderName: line.entryType === 'CREDIT' ? 'NetBanking / Payroll' : 'John Doe',
          recipientName: line.entryType === 'DEBIT' ? 'Merchant / Vendor' : 'John Doe',
          amount: line.amountMinor / 100,
          amountMinor: line.amountMinor,
          currency: 'INR' as const,
          type: line.entryType === 'CREDIT' ? 'TOPUP' : 'TRANSFER',
          status: 'COMPLETED' as const,
          description: line.description || (line.entryType === 'CREDIT' ? 'Credit Posting' : 'Payment Debit'),
          createdAt: line.createdAt || new Date().toISOString(),
        }));
        setTransactions(txns);
      }
    } catch {
      // ignore
    }
  }, [walletId]);

  useEffect(() => {
    fetchLiveWallet();

    const handleUpdate = () => {
      fetchLiveWallet();
    };

    window.addEventListener('payflow:demo-reset', handleUpdate);
    window.addEventListener('payflow:wallet-updated', handleUpdate);

    return () => {
      window.removeEventListener('payflow:demo-reset', handleUpdate);
      window.removeEventListener('payflow:wallet-updated', handleUpdate);
    };
  }, [fetchLiveWallet]);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    await fetchLiveWallet();
    setTimeout(() => {
      setIsRefreshing(false);
    }, 400);
  };

  const handleAddMoney = async (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(addAmount);
    if (isNaN(num) || num <= 0) {
      toastError('Invalid Amount', 'Please enter an amount greater than 0.');
      return;
    }

    setIsSubmitting(true);
    const idempKey = getIdempotencyKey();

    try {
      const res = await topUpWallet(
        walletId,
        {
          amount: num,
          currency: 'INR' as any,
          referenceId: `TOPUP-${Date.now()}`,
          description: `Wallet top-up via ${addMethod}`,
        },
        idempKey
      );

      setBalance(res.balance);
      setIsSubmitting(false);
      setIsAddOpen(false);
      resetIdempotencyKey();
      success('Funds Deposited', `Added ₹${num.toFixed(2)} to your wallet. New Balance: ₹${res.balance.toFixed(2)}`);

      window.dispatchEvent(new CustomEvent('payflow:wallet-updated'));
      await fetchLiveWallet();
    } catch (err: any) {
      setIsSubmitting(false);
      const msg = err.response?.data?.error?.message || err.message || 'Failed to top up wallet';
      toastError('Top-up Failed', msg);
    }
  };

  const handleWithdraw = async (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(withdrawAmount);
    const requestedMinor = toMinorUnits(num);
    const currentBalanceMinor = Math.round(balance * 100);

    if (isNaN(num) || num <= 0) {
      toastError('Invalid Amount', 'Please enter an amount greater than 0.');
      return;
    }

    if (requestedMinor > currentBalanceMinor) {
      toastError('Insufficient Balance', 'Withdrawal amount cannot exceed available balance.');
      return;
    }

    setIsSubmitting(true);
    const idempKey = getIdempotencyKey();

    try {
      const res = await withdrawWallet(
        walletId,
        {
          amount: num,
          currency: 'INR' as any,
          referenceId: `WITHDRAW-${Date.now()}`,
          description: `Withdrawal to ${withdrawBank}`,
        },
        idempKey
      );

      setBalance(res.balance);
      setIsSubmitting(false);
      setIsWithdrawOpen(false);
      resetIdempotencyKey();
      success('Withdrawal Processed', `Withdrew ₹${num.toFixed(2)} to ${withdrawBank}. New Balance: ₹${res.balance.toFixed(2)}`);

      window.dispatchEvent(new CustomEvent('payflow:wallet-updated'));
      await fetchLiveWallet();
    } catch (err: any) {
      setIsSubmitting(false);
      const msg = err.response?.data?.error?.message || err.message || 'Failed to withdraw funds';
      toastError('Withdrawal Failed', msg);
    }
  };

  const balanceMinor = Math.round(balance * 100);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 border-b border-slate-200 pb-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Wallet Management</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time balance, stored liquidity, and atomic debit/credit operations.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={handleRefresh}
            loading={isRefreshing}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin' : ''}`} />}
          >
            Refresh Balance
          </Button>
        </div>
      </div>

      {/* Hero Wallet Card */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <span className="text-xs font-medium uppercase tracking-wider text-slate-500">
              Primary Liquid Balance
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] text-emerald-700 bg-emerald-50 border border-emerald-200 px-1.5 py-0.5 rounded font-medium">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Active
            </span>
          </div>
          <div className="flex items-baseline gap-3 pt-1">
            <MoneyAmount
              amountMinor={balanceMinor}
              currency={currency as any}
              size="kpi"
            />
            <span className="text-xs font-semibold text-slate-400">{currency}</span>
          </div>
          <p className="text-xs text-slate-500 pt-0.5">
            Wallet UUID: <span className="font-mono text-slate-700 font-medium">{walletId}</span>
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Button
            variant="primary"
            onClick={() => setIsAddOpen(true)}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Add Money
          </Button>
          <Button
            variant="secondary"
            onClick={() => setIsWithdrawOpen(true)}
            leftIcon={<ArrowDownLeft className="w-4 h-4" />}
          >
            Withdraw Funds
          </Button>
        </div>
      </div>

      {/* Ledger History */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-900">Wallet Movement History</h2>
          <span className="text-xs text-slate-500 font-mono">
            {transactions.length} verified postings
          </span>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date & Time</TableHead>
              <TableHead>Description / Reference</TableHead>
              <TableHead>Type</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Reference ID</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {transactions.map((txn) => {
              const isCredit = txn.type === 'TOPUP' || txn.recipientName === 'John Doe';
              return (
                <TableRow key={txn.id}>
                  <TableCell className="text-xs text-slate-500 whitespace-nowrap">
                    {formatDateTime(txn.createdAt)}
                  </TableCell>
                  <TableCell>
                    <div className="font-medium text-slate-900 text-xs">
                      {txn.recipientName || txn.senderName || 'External Gateway'}
                    </div>
                    {txn.description && (
                      <div className="text-[11px] text-slate-600 truncate max-w-xs">{txn.description}</div>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="text-xs text-slate-600 font-mono capitalize">
                      {txn.type.toLowerCase()}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <MoneyAmount
                      amountMinor={txn.amountMinor}
                      currency={txn.currency}
                      type={isCredit ? 'credit' : 'debit'}
                      showSign
                      size="sm"
                    />
                  </TableCell>
                  <TableCell className="text-center">
                    <StatusIndicator status={txn.status} size="sm" />
                  </TableCell>
                  <TableCell className="text-right font-mono text-[11px] text-slate-600">
                    {txn.transactionNumber}
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>

      {/* Add Money Modal */}
      <Dialog
        isOpen={isAddOpen}
        onClose={() => setIsAddOpen(false)}
        title="Top-up Wallet Liquidity"
        description="Simulate adding funds to your primary wallet via simulated NetBanking or UPI rail."
      >
        <form onSubmit={handleAddMoney} className="space-y-4 pt-2">
          <Input
            label="Deposit Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            value={addAmount}
            onChange={(e) => setAddAmount(e.target.value)}
            prefix={<span className="text-xs font-semibold text-slate-400">₹</span>}
            required
            autoFocus
          />

          <Select
            label="Payment Source"
            value={addMethod}
            onChange={(e) => setAddMethod(e.target.value)}
            options={[
              { value: 'BANK_TRANSFER', label: 'Instant UPI / NetBanking (Free)' },
              { value: 'DEBIT_CARD', label: 'Corporate Debit Card' },
              { value: 'WIRE_TRANSFER', label: 'RTGS / IMPS Clearing' },
            ]}
          />

          <div className="p-3 bg-slate-50 border border-slate-200 rounded-md text-xs text-slate-600 space-y-1">
            <div className="flex justify-between font-medium">
              <span>Current Balance:</span>
              <span>₹{balance.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-emerald-700 font-medium">
              <span>Added Liquidity:</span>
              <span>+₹{(parseFloat(addAmount) || 0).toFixed(2)}</span>
            </div>
            <div className="border-t border-slate-200 pt-1 flex justify-between font-bold text-slate-900">
              <span>Expected Balance:</span>
              <span>₹{(balance + (parseFloat(addAmount) || 0)).toFixed(2)}</span>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => setIsAddOpen(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              loading={isSubmitting}
              className="bg-emerald-600 hover:bg-emerald-700"
            >
              Confirm Deposit
            </Button>
          </div>
        </form>
      </Dialog>

      {/* Withdraw Modal */}
      <Dialog
        isOpen={isWithdrawOpen}
        onClose={() => setIsWithdrawOpen(false)}
        title="Withdraw Funds to Bank"
        description="Execute an atomic debit to disburse wallet funds directly into your verified bank account."
      >
        <form onSubmit={handleWithdraw} className="space-y-4 pt-2">
          <Input
            label="Withdrawal Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            max={balance}
            value={withdrawAmount}
            onChange={(e) => setWithdrawAmount(e.target.value)}
            prefix={<span className="text-xs font-semibold text-slate-400">₹</span>}
            required
            autoFocus
          />

          <Select
            label="Payout Destination Account"
            value={withdrawBank}
            onChange={(e) => setWithdrawBank(e.target.value)}
            options={[
              { value: 'HDFC Bank •••• 4832', label: 'HDFC Bank (Current A/C •••• 4832)' },
              { value: 'ICICI Bank •••• 9914', label: 'ICICI Bank (Savings •••• 9914)' },
              { value: 'State Bank •••• 1120', label: 'SBI Corporate Clearing (•••• 1120)' },
            ]}
          />

          <div className="p-3 bg-slate-50 border border-slate-200 rounded-md text-xs text-slate-600 space-y-1">
            <div className="flex justify-between font-medium">
              <span>Current Balance:</span>
              <span>₹{balance.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-red-600 font-medium">
              <span>Withdrawal Amount:</span>
              <span>-₹{(parseFloat(withdrawAmount) || 0).toFixed(2)}</span>
            </div>
            <div className="border-t border-slate-200 pt-1 flex justify-between font-bold text-slate-900">
              <span>Remaining Balance:</span>
              <span>₹{Math.max(0, balance - (parseFloat(withdrawAmount) || 0)).toFixed(2)}</span>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
            <Button
              type="button"
              variant="secondary"
              size="sm"
              onClick={() => setIsWithdrawOpen(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              size="sm"
              loading={isSubmitting}
              className="bg-slate-900 hover:bg-slate-800"
            >
              Execute Withdrawal
            </Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
};
