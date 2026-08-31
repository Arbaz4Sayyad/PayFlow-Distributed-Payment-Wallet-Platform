import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { MOCK_WALLET } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const AdminWalletsPage: React.FC = () => {
  const wallets = [
    MOCK_WALLET,
    {
      id: '2cf5e3e1-42fa-5181-9efd-e14bf7f51d3f',
      userId: '2cf5e3e1-42fa-5181-9efd-e14bf7f51d3f',
      currency: 'INR' as const,
      balance: 145000.0,
      balanceMinor: 14500000,
      status: 'ACTIVE' as const,
      createdAt: '2026-08-15T10:00:00Z',
    },
    {
      id: '3da6f4f2-53ab-6292-0fge-f25ca8g62e4a',
      userId: '3da6f4f2-53ab-6292-0fge-f25ca8g62e4a',
      currency: 'INR' as const,
      balance: 894500.0,
      balanceMinor: 89450000,
      status: 'ACTIVE' as const,
      createdAt: '2026-08-01T09:00:00Z',
    },
  ];

  return (
    <div className="space-y-4">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-base font-bold uppercase tracking-wider text-slate-100 font-mono">
          Wallet Master Ledger & Balances
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Real-time balance ledger verified against double-entry journal postings.
        </p>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-md overflow-hidden text-xs">
        <Table className="bg-slate-950 text-slate-200">
          <TableHeader className="bg-slate-900 border-slate-800 text-slate-400">
            <TableRow>
              <TableHead>Wallet ID</TableHead>
              <TableHead>User ID</TableHead>
              <TableHead className="text-right">Balance</TableHead>
              <TableHead className="text-center">Currency</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Created</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-slate-800 font-mono">
            {wallets.map((w) => (
              <TableRow key={w.id} className="hover:bg-slate-900/60">
                <TableCell className="text-amber-400">{w.id}</TableCell>
                <TableCell className="text-slate-400 text-[11px]">{w.userId}</TableCell>
                <TableCell className="text-right">
                  <MoneyAmount amountMinor={w.balanceMinor} currency={w.currency} size="sm" className="text-slate-100" />
                </TableCell>
                <TableCell className="text-center text-slate-300">{w.currency}</TableCell>
                <TableCell className="text-center font-sans">
                  <StatusIndicator status={w.status} size="sm" />
                </TableCell>
                <TableCell className="text-right text-slate-400 text-[11px]">
                  {formatDateTime(w.createdAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
