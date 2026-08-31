import React, { useState } from 'react';
import { Check, X } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../../components/ui/Table';
import { useToast } from '../../components/ui/Toast';
import { MOCK_FRAUD_QUEUE } from '../../mocks/mockData';

export const AdminFraudPage: React.FC = () => {
  const { success, error: toastError } = useToast();
  const [queue, setQueue] = useState(MOCK_FRAUD_QUEUE);

  const handleApprove = (id: string, txnId: string) => {
    setQueue(queue.filter((q) => q.id !== id));
    success('Fraud Flag Overridden', `Transaction ${txnId} approved for release.`);
  };

  const handleReject = (id: string, txnId: string) => {
    setQueue(queue.filter((q) => q.id !== id));
    toastError('Transaction Frozen', `Transaction ${txnId} cancelled and account flagged.`);
  };

  return (
    <div className="space-y-4">
      <div className="border-b border-slate-800 pb-4 flex items-center justify-between">
        <div>
          <h1 className="text-base font-bold uppercase tracking-wider text-slate-100 font-mono">
            Fraud Risk Engine & Review Queue
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Rule-based velocity, machine-learning anomaly detection, and manual review triggers.
          </p>
        </div>
      </div>

      <div className="bg-slate-950 border border-slate-800 rounded-md overflow-hidden">
        <Table className="bg-slate-950 text-slate-200">
          <TableHeader className="bg-slate-900 border-slate-800 text-slate-400">
            <TableRow>
              <TableHead>Transaction</TableHead>
              <TableHead>Account User</TableHead>
              <TableHead className="text-right">Amount</TableHead>
              <TableHead className="text-center">Risk Level</TableHead>
              <TableHead>Triggered Rule & Reason</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody className="divide-slate-800">
            {queue.length > 0 ? (
              queue.map((item) => (
                <TableRow key={item.id} className="hover:bg-slate-900/60">
                  <TableCell className="font-mono text-amber-400 font-semibold text-xs">
                    {item.transactionId}
                  </TableCell>
                  <TableCell className="text-xs text-slate-300 font-mono">{item.userEmail}</TableCell>
                  <TableCell className="text-right">
                    <MoneyAmount
                      amountMinor={item.amountMinor}
                      currency={item.currency}
                      size="sm"
                      className="text-slate-100"
                    />
                  </TableCell>
                  <TableCell className="text-center">
                    <span className="text-[10px] font-mono font-bold bg-red-950 text-red-400 border border-red-800 px-2 py-0.5 rounded">
                      {item.riskScore}
                    </span>
                  </TableCell>
                  <TableCell className="text-xs text-slate-300">
                    <p className="font-medium">{item.reason}</p>
                    <div className="flex gap-1 mt-0.5">
                      {item.triggeredRules.map((rule) => (
                        <span key={rule} className="text-[9px] font-mono bg-slate-800 text-slate-400 px-1 py-0.5 rounded">
                          {rule}
                        </span>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-1.5">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-emerald-400 hover:text-emerald-300 hover:bg-emerald-950/40 h-7 px-2"
                        onClick={() => handleApprove(item.id, item.transactionId)}
                      >
                        <Check className="w-3.5 h-3.5 mr-1" />
                        Release
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-red-400 hover:text-red-300 hover:bg-red-950/40 h-7 px-2"
                        onClick={() => handleReject(item.id, item.transactionId)}
                      >
                        <X className="w-3.5 h-3.5 mr-1" />
                        Reject
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-12 text-slate-500 font-mono">
                  No flagged transactions in review queue. Risk engine operating with clean threshold.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};
