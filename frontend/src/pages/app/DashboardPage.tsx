import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  ArrowUpRight,
  Plus,
  ArrowDownLeft,
  ArrowRight,
  ShieldCheck,
  RefreshCw,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';
import { MOCK_WALLET, MOCK_TRANSACTIONS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [wallet] = useState(MOCK_WALLET);
  const [transactions] = useState(MOCK_TRANSACTIONS);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = async () => {
    setIsRefreshing(true);
    setTimeout(() => {
      setIsRefreshing(false);
    }, 400);
  };

  const displayName = user?.firstName || user?.email?.split('@')[0] || 'User';

  return (
    <div className="space-y-6">
      {/* Overview Greeting */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 border-b border-slate-200 pb-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Overview</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Good day, {displayName}. Your payment system is operating normally.
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
            Refresh Data
          </Button>
        </div>
      </div>

      {/* Main Financial Balance KPI Card */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium uppercase tracking-wider text-slate-500">
                Available Wallet Balance
              </span>
              <span className="inline-flex items-center gap-1 text-[11px] text-emerald-700 bg-emerald-50 border border-emerald-200 px-1.5 py-0.5 rounded font-medium">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                Live Ledger
              </span>
            </div>
            <div className="flex items-baseline gap-3 pt-1">
              <MoneyAmount
                amountMinor={wallet.balanceMinor}
                currency={wallet.currency}
                size="kpi"
              />
              <span className="text-xs font-semibold text-slate-400">{wallet.currency}</span>
            </div>
            <p className="text-xs text-slate-500 pt-0.5">
              Wallet ID: <span className="font-mono text-slate-700 font-medium">••••••••{wallet.id.slice(-4)}</span>
            </p>
          </div>

          {/* Core Money Actions */}
          <div className="flex flex-wrap items-center gap-2.5">
            <Button
              variant="primary"
              onClick={() => navigate(ROUTES.TRANSFERS)}
              leftIcon={<ArrowUpRight className="w-4 h-4" />}
            >
              Send Money
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate(ROUTES.WALLET)}
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Add Money
            </Button>
            <Button
              variant="secondary"
              onClick={() => navigate(ROUTES.WALLET)}
              leftIcon={<ArrowDownLeft className="w-4 h-4" />}
            >
              Withdraw
            </Button>
          </div>
        </div>
      </div>

      {/* Recent Transactions Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold text-slate-900">Recent Transactions</h2>
          <Link
            to={ROUTES.TRANSACTIONS}
            className="text-xs font-medium text-blue-600 hover:text-blue-700 hover:underline flex items-center gap-1"
          >
            <span>View full ledger</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date & Time</TableHead>
              <TableHead>Description / Party</TableHead>
              <TableHead>Type</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Reference</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {transactions.slice(0, 5).map((txn) => {
              const isCredit = txn.type === 'TOPUP' || txn.recipientName === 'Arbaz Sayyad';
              return (
                <TableRow
                  key={txn.id}
                  isClickable
                  onClick={() => navigate(ROUTES.TRANSACTION_DETAIL(txn.id))}
                >
                  <TableCell className="text-xs text-slate-500 whitespace-nowrap">
                    {formatDateTime(txn.createdAt)}
                  </TableCell>
                  <TableCell>
                    <div className="font-medium text-slate-900 text-xs">
                      {txn.recipientName || txn.senderName || 'External Transaction'}
                    </div>
                    {txn.description && (
                      <div className="text-[11px] text-slate-600 truncate max-w-xs">{txn.description}</div>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="text-xs text-slate-600 font-mono capitalize">
                      {txn.type.replace('_', ' ').toLowerCase()}
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

      {/* Security and Settlement Invariant Notice */}
      <div className="bg-slate-100/70 border border-slate-200 rounded-md p-4 flex items-start gap-3">
        <ShieldCheck className="w-5 h-5 text-slate-600 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-600 space-y-0.5">
          <p className="font-semibold text-slate-800">Double-Entry Ledger Protected</p>
          <p>
            Every transaction is backed by balanced double-entry journal postings with end-to-end Saga orchestration. All transfers are protected against double-execution with cryptographic idempotency keys.
          </p>
        </div>
      </div>
    </div>
  );
};
