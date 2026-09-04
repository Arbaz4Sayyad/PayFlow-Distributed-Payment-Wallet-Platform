import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Search,
  Download,
  ChevronLeft,
  ChevronRight,
  RefreshCw,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { apiClient } from '../../api/client';
import { DEMO_CONFIG } from '../../api/demo';
import { formatDateTime } from '../../utils/dates';
import { useDebounce } from '../../hooks/useDebounce';
import { Transaction } from '../../types';

export const TransactionsPage: React.FC = () => {
  const walletId = DEMO_CONFIG.primaryUser.walletId;

  // Filter States
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useDebounce(searchTerm, 250);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const itemsPerPage = 8;

  const fetchTransactions = useCallback(async () => {
    setIsLoading(true);
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
      // fallback
    } finally {
      setIsLoading(false);
    }
  }, [walletId]);

  useEffect(() => {
    fetchTransactions();

    const handleUpdate = () => fetchTransactions();
    window.addEventListener('payflow:demo-reset', handleUpdate);
    window.addEventListener('payflow:wallet-updated', handleUpdate);

    return () => {
      window.removeEventListener('payflow:demo-reset', handleUpdate);
      window.removeEventListener('payflow:wallet-updated', handleUpdate);
    };
  }, [fetchTransactions]);

  // Filter logic
  const filteredTransactions = useMemo(() => {
    return transactions.filter((txn) => {
      // Search match
      const matchSearch =
        debouncedSearch === '' ||
        txn.transactionNumber.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
        (txn.description && txn.description.toLowerCase().includes(debouncedSearch.toLowerCase())) ||
        (txn.recipientName && txn.recipientName.toLowerCase().includes(debouncedSearch.toLowerCase())) ||
        (txn.senderName && txn.senderName.toLowerCase().includes(debouncedSearch.toLowerCase()));

      // Type match
      const matchType = typeFilter === 'ALL' || txn.type === typeFilter;

      // Status match
      const matchStatus = statusFilter === 'ALL' || txn.status === statusFilter;

      return matchSearch && matchType && matchStatus;
    });
  }, [transactions, debouncedSearch, typeFilter, statusFilter]);

  const totalPages = Math.ceil(filteredTransactions.length / itemsPerPage) || 1;
  const paginatedData = filteredTransactions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-200 pb-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Transaction Ledger</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Immutable double-entry journal records and settlement history.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={fetchTransactions}
            loading={isLoading}
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
          >
            Refresh
          </Button>
          <Button
            variant="secondary"
            size="sm"
            leftIcon={<Download className="w-3.5 h-3.5" />}
          >
            Export CSV
          </Button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white border border-slate-200 rounded-md p-3 shadow-subtle flex flex-col sm:flex-row items-center gap-3">
        <div className="w-full sm:flex-1">
          <Input
            placeholder="Search by ID, party, or description..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            prefix={<Search className="w-4 h-4 text-slate-400" />}
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <div className="w-40">
            <Select
              value={typeFilter}
              onChange={(e) => {
                setTypeFilter(e.target.value);
                setCurrentPage(1);
              }}
              options={[
                { value: 'ALL', label: 'All Types' },
                { value: 'TRANSFER', label: 'Transfers' },
                { value: 'TOPUP', label: 'Deposits / Top-Ups' },
                { value: 'WITHDRAW', label: 'Withdrawals' },
              ]}
            />
          </div>

          <div className="w-36">
            <Select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setCurrentPage(1);
              }}
              options={[
                { value: 'ALL', label: 'All Statuses' },
                { value: 'COMPLETED', label: 'Completed' },
                { value: 'PROCESSING', label: 'Processing' },
                { value: 'FAILED', label: 'Failed' },
              ]}
            />
          </div>
        </div>
      </div>

      {/* Ledger Table */}
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Date & Time</TableHead>
            <TableHead>Description / Party</TableHead>
            <TableHead>Type</TableHead>
            <TableHead className="text-right">Amount</TableHead>
            <TableHead className="text-center">Status</TableHead>
            <TableHead className="text-right">Reference ID</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {paginatedData.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center py-8 text-xs text-slate-500">
                No matching double-entry ledger transactions found.
              </TableCell>
            </TableRow>
          ) : (
            paginatedData.map((txn) => {
              const isCredit = txn.type === 'TOPUP' || txn.recipientName === 'John Doe';
              return (
                <TableRow
                  key={txn.id}
                  isClickable
                >
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
            })
          )}
        </TableBody>
      </Table>

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between text-xs text-slate-500 px-1 pt-2">
          <div>
            Showing {(currentPage - 1) * itemsPerPage + 1} to{' '}
            {Math.min(currentPage * itemsPerPage, filteredTransactions.length)} of {filteredTransactions.length} entries
          </div>
          <div className="flex items-center gap-1">
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === 1}
              onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
            >
              <ChevronLeft className="w-3.5 h-3.5" />
            </Button>
            <span className="px-2 font-medium text-slate-700">
              Page {currentPage} of {totalPages}
            </span>
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage === totalPages}
              onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
            >
              <ChevronRight className="w-3.5 h-3.5" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
