import React from 'react';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { MOCK_USER } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const AdminUsersPage: React.FC = () => {
  const users = [
    MOCK_USER,
    {
      id: '2cf5e3e1-42fa-5181-9efd-e14bf7f51d3f',
      email: 'rahul.sharma@payflow.com',
      phone: '+919811223344',
      firstName: 'Rahul',
      lastName: 'Sharma',
      role: 'ROLE_USER' as const,
      status: 'ACTIVE' as const,
      kycLevel: 'TIER_2' as const,
      createdAt: '2026-08-15T10:00:00Z',
    },
    {
      id: '3da6f4f2-53ab-6292-0fge-f25ca8g62e4a',
      email: 'merchant.partner@amazon.in',
      phone: '+919877001122',
      firstName: 'Amazon',
      lastName: 'India Direct',
      role: 'ROLE_MERCHANT' as const,
      status: 'ACTIVE' as const,
      kycLevel: 'TIER_3' as const,
      createdAt: '2026-08-01T09:00:00Z',
    },
  ];

  return (
    <div className="space-y-4">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-base font-bold uppercase tracking-wider text-slate-100 font-mono">
          User Account Directory
        </h1>
        <p className="text-xs text-slate-400 mt-0.5">
          Directory of registered retail customers, merchant partners, and platform administrators.
        </p>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-md overflow-hidden text-xs">
        <Table className="bg-slate-950 text-slate-200">
          <TableHeader className="bg-slate-900 border-slate-800 text-slate-400">
            <TableRow>
              <TableHead>User ID (UUID)</TableHead>
              <TableHead>Name / Email</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead>Role</TableHead>
              <TableHead className="text-center">KYC Level</TableHead>
              <TableHead className="text-center">Status</TableHead>
              <TableHead className="text-right">Registered At</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-slate-800">
            {users.map((u) => (
              <TableRow key={u.id} className="hover:bg-slate-900/60 font-mono">
                <TableCell className="text-slate-400 text-[11px]">{u.id}</TableCell>
                <TableCell className="font-sans">
                  <span className="font-semibold text-slate-100 block">{u.firstName} {u.lastName}</span>
                  <span className="text-[11px] text-slate-400 font-mono">{u.email}</span>
                </TableCell>
                <TableCell className="text-slate-300">{u.phone}</TableCell>
                <TableCell>
                  <span className="text-[10px] bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded">
                    {u.role}
                  </span>
                </TableCell>
                <TableCell className="text-center">
                  <span className="text-[10px] bg-blue-950 text-blue-300 border border-blue-800 px-1.5 py-0.5 rounded font-bold">
                    {u.kycLevel}
                  </span>
                </TableCell>
                <TableCell className="text-center font-sans">
                  <StatusIndicator status={u.status} size="sm" />
                </TableCell>
                <TableCell className="text-right text-slate-400 text-[11px]">
                  {formatDateTime(u.createdAt)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
