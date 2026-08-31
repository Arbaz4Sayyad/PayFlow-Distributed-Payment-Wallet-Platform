import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  CheckCircle2,
  Clock,
  ShieldCheck,
  Copy,
} from 'lucide-react';
import { MoneyAmount } from '../../components/ui/MoneyAmount';
import { StatusIndicator } from '../../components/ui/StatusIndicator';
import { useToast } from '../../components/ui/Toast';
import { ROUTES } from '../../constants/routes';
import { MOCK_TRANSACTIONS } from '../../mocks/mockData';
import { formatDateTime } from '../../utils/dates';

export const TransactionDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { success } = useToast();

  const txn = MOCK_TRANSACTIONS.find((t) => t.id === id) || MOCK_TRANSACTIONS[0];

  const handleCopy = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    success('Copied to Clipboard', `${label} copied.`);
  };

  // Distributed Saga execution lifecycle timeline
  const sagaSteps = [
    { label: 'Payment Intent Created', desc: 'Idempotency validated in payment-service', status: 'COMPLETED' },
    { label: 'Fraud Detection Evaluated', desc: 'Risk evaluation passed with score LOW', status: 'COMPLETED' },
    { label: 'Wallet Balance Debited', desc: 'Optimistic lock verified in wallet-service', status: 'COMPLETED' },
    { label: 'Double-Entry Ledger Recorded', desc: 'Balanced journal entry posted in ledger-service', status: 'COMPLETED' },
    { label: 'Event Published & Settled', desc: 'Transactional outbox event dispatched to Kafka', status: txn.status === 'PROCESSING' ? 'PENDING' : 'COMPLETED' },
  ];

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      {/* Back button */}
      <button
        onClick={() => navigate(ROUTES.TRANSACTIONS)}
        className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-900 transition-colors"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Back to Transaction Ledger</span>
      </button>

      {/* Main Header Summary */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <span className="text-xs font-mono text-slate-400 uppercase tracking-wider">
              Transaction Details
            </span>
            <div className="flex items-center gap-2.5 mt-0.5">
              <h1 className="text-lg font-bold font-mono text-slate-900">{txn.transactionNumber}</h1>
              <button
                onClick={() => handleCopy(txn.transactionNumber, 'Transaction ID')}
                className="text-slate-400 hover:text-slate-600"
                title="Copy reference"
              >
                <Copy className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
          <StatusIndicator status={txn.status} />
        </div>

        {/* Big Amount Figure */}
        <div className="py-2">
          <span className="text-xs text-slate-500">Amount Settled</span>
          <div className="flex items-baseline gap-2 mt-1">
            <MoneyAmount amountMinor={txn.amountMinor} currency={txn.currency} size="kpi" />
            <span className="text-sm font-semibold text-slate-400">{txn.currency}</span>
          </div>
        </div>

        {/* Key-Value Breakdown Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-slate-100 text-xs">
          <div>
            <span className="text-slate-500">Sender Party</span>
            <p className="font-semibold text-slate-900 mt-0.5">{txn.senderName || 'Self'}</p>
            <p className="font-mono text-[11px] text-slate-400 truncate">{txn.senderWalletId}</p>
          </div>

          <div>
            <span className="text-slate-500">Recipient Party</span>
            <p className="font-semibold text-slate-900 mt-0.5">{txn.recipientName || 'PayFlow'}</p>
            <p className="font-mono text-[11px] text-slate-400 truncate">{txn.recipientWalletId || 'Internal Ledger'}</p>
          </div>

          <div>
            <span className="text-slate-500">Timestamp</span>
            <p className="font-medium text-slate-900 mt-0.5">{formatDateTime(txn.createdAt)}</p>
          </div>

          <div>
            <span className="text-slate-500">Payment Type / Method</span>
            <p className="font-medium text-slate-900 mt-0.5 capitalize">{txn.type.replace('_', ' ').toLowerCase()}</p>
          </div>

          {txn.description && (
            <div className="sm:col-span-2">
              <span className="text-slate-500">Description / Note</span>
              <p className="font-medium text-slate-800 mt-0.5">{txn.description}</p>
            </div>
          )}
        </div>
      </div>

      {/* Distributed Saga Execution Timeline */}
      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle space-y-4">
        <h2 className="text-sm font-semibold text-slate-900 flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-slate-700" />
          <span>Distributed Saga Settlement Lifecycle</span>
        </h2>

        <div className="space-y-4 pt-2">
          {sagaSteps.map((step, idx) => {
            const isDone = step.status === 'COMPLETED';
            return (
              <div key={step.label} className="flex items-start gap-3 relative">
                {idx < sagaSteps.length - 1 && (
                  <div className="absolute left-2.5 top-6 bottom-0 w-px bg-slate-200" />
                )}
                <div
                  className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] shrink-0 ${
                    isDone
                      ? 'bg-emerald-500 text-white'
                      : 'bg-amber-100 text-amber-600 border border-amber-300'
                  }`}
                >
                  {isDone ? <CheckCircle2 className="w-3 h-3" /> : <Clock className="w-3 h-3" />}
                </div>
                <div className="text-xs">
                  <p className={`font-semibold ${isDone ? 'text-slate-900' : 'text-amber-700'}`}>
                    {step.label}
                  </p>
                  <p className="text-[11px] text-slate-500">{step.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
