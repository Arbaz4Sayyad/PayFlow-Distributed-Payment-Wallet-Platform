import React, { useState } from 'react';
import {
  Plus,
  ArrowDownLeft,
  RefreshCw,
  Building,
  CheckCircle2,
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
import { MOCK_WALLET, MOCK_TRANSACTIONS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';
import { toMinorUnits } from '../../utils/currency';

export const WalletPage: React.FC = () => {
  const { success, error: toastError } = useToast();
  const { getIdempotencyKey, resetIdempotencyKey } = useIdempotency();

  const [wallet, setWallet] = useState(MOCK_WALLET);
  const [transactions, setTransactions] = useState(MOCK_TRANSACTIONS);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Modals
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form states
  const [addAmount, setAddAmount] = useState('5000.00');
  const [addMethod, setAddMethod] = useState('BANK_TRANSFER');
  const [withdrawAmount, setWithdrawAmount] = useState('2000.00');
  const [withdrawBank, setWithdrawBank] = useState('HDFC Bank •••• 4832');

  const handleRefresh = () => {
    setIsRefreshing(true);
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
    getIdempotencyKey();

    setTimeout(() => {
      const addedMinor = toMinorUnits(num);
      const newBalanceMinor = wallet.balanceMinor + addedMinor;
      setWallet({
        ...wallet,
        balanceMinor: newBalanceMinor,
        balance: newBalanceMinor / 100,
      });

      const newTxn = {
        id: 'txn_' + Math.random().toString(36).substring(2, 9),
        transactionNumber: 'TXN-' + Math.floor(10000 + Math.random() * 90000),
        senderWalletId: 'SYSTEM_TOPUP_GATEWAY',
        recipientWalletId: wallet.id,
        senderName: 'NetBanking Deposit',
        recipientName: 'Arbaz Sayyad',
        amount: num,
        amountMinor: addedMinor,
        currency: wallet.currency,
        type: 'TOPUP' as const,
        status: 'COMPLETED' as const,
        description: 'Wallet top-up via ' + addMethod,
        createdAt: new Date().toISOString(),
      };

      setTransactions([newTxn, ...transactions]);
      setIsSubmitting(false);
      setIsAddOpen(false);
      resetIdempotencyKey();
      success('Funds Deposited', `Added ₹${num.toFixed(2)} to your wallet.`);
    }, 600);
  };

  const handleWithdraw = async (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(withdrawAmount);
    const requestedMinor = toMinorUnits(num);

    if (isNaN(num) || num <= 0) {
      toastError('Invalid Amount', 'Please enter an amount greater than 0.');
      return;
    }

    if (requestedMinor > wallet.balanceMinor) {
      toastError('Insufficient Balance', 'Withdrawal amount cannot exceed available balance.');
      return;
    }

    setIsSubmitting(true);
    getIdempotencyKey();

    setTimeout(() => {
      const newBalanceMinor = wallet.balanceMinor - requestedMinor;
      setWallet({
        ...wallet,
        balanceMinor: newBalanceMinor,
        balance: newBalanceMinor / 100,
      });

      const newTxn = {
        id: 'txn_' + Math.random().toString(36).substring(2, 9),
        transactionNumber: 'TXN-' + Math.floor(10000 + Math.random() * 90000),
        senderWalletId: wallet.id,
        recipientWalletId: 'BANK_ESCROW_OUT',
        senderName: 'Arbaz Sayyad',
        recipientName: withdrawBank,
        amount: num,
        amountMinor: requestedMinor,
        currency: wallet.currency,
        type: 'WITHDRAW' as const,
        status: 'COMPLETED' as const,
        description: 'Withdrawal to ' + withdrawBank,
        createdAt: new Date().toISOString(),
      };

      setTransactions([newTxn, ...transactions]);
      setIsSubmitting(false);
      setIsWithdrawOpen(false);
      resetIdempotencyKey();
      success('Withdrawal Initiated', `₹${num.toFixed(2)} transferred to your bank.`);
    }, 600);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 border-b border-slate-200 pb-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Wallet & Balances</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage your primary treasury balance and settlement methods.
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
            Refresh
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsWithdrawOpen(true)}
            leftIcon={<ArrowDownLeft className="w-3.5 h-3.5" />}
          >
            Withdraw
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setIsAddOpen(true)}
            leftIcon={<Plus className="w-3.5 h-3.5" />}
          >
            Add Money
          </Button>
        </div>
      </div>

      {/* Main Wallet Info Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-subtle md:col-span-2 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">
              Available Balance
            </span>
            <StatusIndicator status={wallet.status} size="sm" />
          </div>
          <div className="flex items-baseline gap-2">
            <MoneyAmount
              amountMinor={wallet.balanceMinor}
              currency={wallet.currency}
              size="kpi"
            />
            <span className="text-sm font-semibold text-slate-400">{wallet.currency}</span>
          </div>
          <div className="pt-2 border-t border-slate-100 flex flex-wrap items-center justify-between text-xs text-slate-500 gap-2">
            <div>
              Wallet ID: <span className="font-mono text-slate-800 font-medium">••••••••{wallet.id.slice(-4)}</span>
            </div>
            <div>
              Created: <span className="text-slate-800 font-medium">12 Aug 2026</span>
            </div>
          </div>
        </div>

        {/* Linked Settlement Method */}
        <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-subtle flex flex-col justify-between space-y-3">
          <div>
            <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">
              Primary Settlement Account
            </span>
            <div className="flex items-center gap-3 mt-2">
              <div className="p-2 bg-slate-100 rounded-md text-slate-700">
                <Building className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-semibold text-slate-900">HDFC Bank</p>
                <p className="text-[11px] font-mono text-slate-500">•••• •••• 4832</p>
              </div>
            </div>
          </div>
          <div className="text-[11px] text-emerald-600 font-medium flex items-center gap-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Instant IMPS / NEFT Enabled</span>
          </div>
        </div>
      </div>

      {/* Balance Activity Table */}
      <div className="space-y-3">
        <h2 className="text-sm font-semibold text-slate-900">Balance Activity</h2>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date</TableHead>
              <TableHead>Activity Details</TableHead>
              <TableHead>Type</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="text-center">Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {transactions.map((txn) => {
              const isCredit = txn.type === 'TOPUP' || txn.recipientName === 'Arbaz Sayyad';
              return (
                <TableRow key={txn.id}>
                  <TableCell className="text-xs text-slate-500 whitespace-nowrap">
                    {formatDateTime(txn.createdAt)}
                  </TableCell>
                  <TableCell>
                    <div className="font-medium text-slate-900 text-xs">{txn.description || txn.type}</div>
                    <div className="text-[11px] font-mono text-slate-400">{txn.transactionNumber}</div>
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
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>

      {/* Add Money Modal Dialog */}
      <Dialog
        isOpen={isAddOpen}
        onClose={() => !isSubmitting && setIsAddOpen(false)}
        title="Add Money to Wallet"
        description="Top up your available wallet balance via instant bank payment."
        footer={
          <>
            <Button
              variant="secondary"
              size="sm"
              disabled={isSubmitting}
              onClick={() => setIsAddOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={isSubmitting}
              disabled={isSubmitting}
              onClick={handleAddMoney}
            >
              Confirm Deposit
            </Button>
          </>
        }
      >
        <form onSubmit={handleAddMoney} className="space-y-4">
          <Input
            label="Deposit Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            required
            value={addAmount}
            onChange={(e) => setAddAmount(e.target.value)}
            prefix={<span className="text-slate-500 font-medium">₹</span>}
            description="Minimum deposit: ₹1.00"
          />

          <Select
            label="Payment Method"
            value={addMethod}
            onChange={(e) => setAddMethod(e.target.value)}
            options={[
              { value: 'BANK_TRANSFER', label: 'Direct NetBanking (Instant)' },
              { value: 'UPI', label: 'Unified Payments Interface (UPI)' },
              { value: 'DEBIT_CARD', label: 'Debit Card (Visa/Mastercard)' },
            ]}
          />
        </form>
      </Dialog>

      {/* Withdraw Modal Dialog */}
      <Dialog
        isOpen={isWithdrawOpen}
        onClose={() => !isSubmitting && setIsWithdrawOpen(false)}
        title="Withdraw Funds"
        description="Transfer funds from your PayFlow wallet to your registered bank account."
        footer={
          <>
            <Button
              variant="secondary"
              size="sm"
              disabled={isSubmitting}
              onClick={() => setIsWithdrawOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={isSubmitting}
              disabled={isSubmitting}
              onClick={handleWithdraw}
            >
              Confirm Withdrawal
            </Button>
          </>
        }
      >
        <form onSubmit={handleWithdraw} className="space-y-4">
          <div className="bg-slate-50 p-3 rounded border border-slate-200 text-xs flex justify-between items-center">
            <span className="text-slate-500">Available to withdraw:</span>
            <span className="font-semibold text-slate-900">
              <MoneyAmount amountMinor={wallet.balanceMinor} currency={wallet.currency} size="sm" />
            </span>
          </div>

          <Input
            label="Withdrawal Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            required
            value={withdrawAmount}
            onChange={(e) => setWithdrawAmount(e.target.value)}
            prefix={<span className="text-slate-500 font-medium">₹</span>}
          />

          <Select
            label="Destination Account"
            value={withdrawBank}
            onChange={(e) => setWithdrawBank(e.target.value)}
            options={[
              { value: 'HDFC Bank •••• 4832', label: 'HDFC Bank •••• 4832 (Primary)' },
              { value: 'ICICI Bank •••• 9912', label: 'ICICI Bank •••• 9912' },
            ]}
          />
        </form>
      </Dialog>
    </div>
  );
};
