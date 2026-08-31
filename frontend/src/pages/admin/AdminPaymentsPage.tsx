import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { MOCK_TRANSACTIONS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const AdminPaymentsPage: React.FC = () => {
  return (
    <div className="space-y-4">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-base font-bold uppercase tracking-wider text-slate-100 font-mono">
          Distributed Payment Sagas
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          End-to-end orchestration states, idempotency records, and compensating transactions.
        </p>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-md overflow-hidden text-xs">
        <Table className="bg-slate-950 text-slate-200">
          <TableHeader className="bg-slate-900 border-slate-800 text-slate-400">
            <TableRow>
              <TableHead>Payment Reference</TableHead>
              <TableHead>Parties</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead>Type</TableHead>
              <TableHead className="text-center">Saga State</TableHead>
              <TableHead className="text-right">Executed</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-slate-800 font-mono">
            {MOCK_TRANSACTIONS.map((txn) => (
              <TableRow key={txn.id} className="hover:bg-slate-900/60">
                <TableCell className="text-emerald-400 font-semibold">{txn.transactionNumber}</TableCell>
                <TableCell className="text-slate-300 font-sans text-xs">
                  {txn.senderName || 'Wallet'} → {txn.recipientName || 'External'}
                </TableCell>
                <TableCell className="text-right">
                  <MoneyAmount amountMinor={txn.amountMinor} currency={txn.currency} size="sm" className="text-slate-100" />
                </TableCell>
                <TableCell className="text-slate-400 uppercase text-[10px]">{txn.type}</TableCell>
                <TableCell className="text-center font-sans">
                  <StatusIndicator status={txn.status} size="sm" />
                </TableCell>
                <TableCell className="text-right text-slate-400 text-[11px]">
                  {formatDateTime(txn.createdAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
