import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  Download,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { ROUTES } from '../../constants/routes';
import { MOCK_TRANSACTIONS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';
import { useDebounce } from '../../hooks/useDebounce';

export const TransactionsPage: React.FC = () => {
  const navigate = useNavigate();

  // Filter States
  const [searchTerm, setSearchTerm] = useState('');
  const debouncedSearch = useDebounce(searchTerm, 250);
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 8;

  // Filter logic
  const filteredTransactions = useMemo(() => {
    return MOCK_TRANSACTIONS.filter((txn) => {
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
  }, [debouncedSearch, typeFilter, statusFilter]);

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
                { value: 'MERCHANT_PAYMENT', label: 'Merchant Payments' },
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
                { value: 'REFUNDED', label: 'Refunded' },
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
            <TableHead>Counterparty / Description</TableHead>
            <TableHead>Type</TableHead>
            <TableHead className="text-right">Amount</TableHead>
            <TableHead className="text-center">Status</TableHead>
            <TableHead className="text-right">Transaction Reference</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {paginatedData.length > 0 ? (
            paginatedData.map((txn) => {
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
                      <div className="text-[11px] text-slate-600 truncate max-w-sm">{txn.description}</div>
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
                  <TableCell className="text-right font-mono text-[11px] text-slate-600 font-medium">
                    {txn.transactionNumber}
                  </TableCell>
                </TableRow>
              );
            })
          ) : (
            <TableRow>
              <TableCell colSpan={6} className="text-center py-12 text-slate-500">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-slate-700">No transactions found</p>
                  <p className="text-xs text-slate-400">
                    Try adjusting your search terms or filter selection.
                  </p>
                </div>
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>

      {/* Pagination Footer */}
      <div className="flex items-center justify-between px-1 text-xs text-slate-500">
        <div>
          Showing{' '}
          <span className="font-medium text-slate-900">
            {filteredTransactions.length === 0 ? 0 : (currentPage - 1) * itemsPerPage + 1}
          </span>{' '}
          to{' '}
          <span className="font-medium text-slate-900">
            {Math.min(currentPage * itemsPerPage, filteredTransactions.length)}
          </span>{' '}
          of <span className="font-medium text-slate-900">{filteredTransactions.length}</span> records
        </div>

        <div className="flex items-center gap-1">
          <Button
            variant="secondary"
            size="sm"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
            leftIcon={<ChevronLeft className="w-3.5 h-3.5" />}
          >
            Prev
          </Button>
          <span className="px-2 font-mono text-slate-700">
            {currentPage} / {totalPages}
          </span>
          <Button
            variant="secondary"
            size="sm"
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
            rightIcon={<ChevronRight className="w-3.5 h-3.5" />}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
};
