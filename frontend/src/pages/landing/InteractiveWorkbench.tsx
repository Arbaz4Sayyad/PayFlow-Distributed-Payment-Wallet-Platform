import React, { useState } from 'react';
import { Wallet, ArrowLeftRight, FileText, ShieldCheck, Check } from 'lucide-react';
import { MoneyAmount } from '../../components/ui/MoneyAmount';

type TabKey = 'wallet' | 'transfers' | 'ledger' | 'security';

export const InteractiveWorkbench: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabKey>('wallet');

  return (
    <section id="workbench" className="py-16 md:py-24 bg-slate-50/50 border-b border-slate-200/70">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
            Everything you need to manage money.
          </h2>
          <p className="text-xs sm:text-sm text-slate-600 mt-2">
            Explore the core features that power everyday payments and account management.
          </p>
        </div>

        {/* Tab Navigation Controls */}
        <div className="flex items-center justify-center mb-8">
          <div className="inline-flex p-1 bg-slate-100 rounded-lg border border-slate-200/80 max-w-full overflow-x-auto">
            <button
              onClick={() => setActiveTab('wallet')}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-xs font-medium transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'wallet'
                  ? 'bg-white text-slate-900 shadow-sm border border-slate-200/80 font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Wallet className="w-3.5 h-3.5 text-slate-700" />
              <span>Digital Wallet</span>
            </button>
            <button
              onClick={() => setActiveTab('transfers')}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-xs font-medium transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'transfers'
                  ? 'bg-white text-slate-900 shadow-sm border border-slate-200/80 font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ArrowLeftRight className="w-3.5 h-3.5 text-slate-700" />
              <span>Instant Transfers</span>
            </button>
            <button
              onClick={() => setActiveTab('ledger')}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-xs font-medium transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'ledger'
                  ? 'bg-white text-slate-900 shadow-sm border border-slate-200/80 font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <FileText className="w-3.5 h-3.5 text-slate-700" />
              <span>Ledger History</span>
            </button>
            <button
              onClick={() => setActiveTab('security')}
              className={`flex items-center gap-2 px-4 py-2 rounded-md text-xs font-medium transition-all cursor-pointer whitespace-nowrap ${
                activeTab === 'security'
                  ? 'bg-white text-slate-900 shadow-sm border border-slate-200/80 font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <ShieldCheck className="w-3.5 h-3.5 text-slate-700" />
              <span>Safety & Protection</span>
            </button>
          </div>
        </div>

        {/* Tab Content Panels */}
        <div className="bg-white border border-slate-200 rounded-xl p-6 sm:p-8 shadow-card">
          {/* TAB 1: Digital Wallet */}
          {activeTab === 'wallet' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center text-left">
              <div className="lg:col-span-5 space-y-4">
                <h3 className="text-xl font-bold text-slate-900">
                  A simple wallet for all your funds
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Hold balances securely in INR. Top up from your bank whenever you want, and withdraw funds with immediate balance updates.
                </p>
                <div className="space-y-2.5 text-xs text-slate-600 pt-1">
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Instant top-up and withdrawal anytime</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Real-time balance updates on every transaction</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Strict overdraft protection prevents negative balances</span>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-7 bg-slate-50 border border-slate-200 rounded-lg p-5 space-y-4">
                <div className="flex items-center justify-between border-b border-slate-200/80 pb-3">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-emerald-500" />
                    <span className="text-xs font-semibold text-slate-900">Primary Wallet</span>
                  </div>
                  <span className="text-xs text-slate-500">ID: ••••4821</span>
                </div>
                <div className="bg-white border border-slate-200 p-4 rounded-lg">
                  <span className="text-xs text-slate-500">Available Balance</span>
                  <div className="flex items-baseline gap-2 mt-1">
                    <MoneyAmount amountMinor={2485050} currency="INR" size="kpi" />
                    <span className="text-xs font-semibold text-slate-400">INR</span>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div className="p-3 bg-white border border-slate-200 rounded text-center">
                    <span className="text-slate-500">Recent Deposit</span>
                    <div className="font-semibold text-slate-900 mt-0.5">+₹5,000.00</div>
                  </div>
                  <div className="p-3 bg-white border border-slate-200 rounded text-center">
                    <span className="text-slate-500">Recent Withdrawal</span>
                    <div className="font-semibold text-slate-900 mt-0.5">-₹500.00</div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: Instant Transfers */}
          {activeTab === 'transfers' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center text-left">
              <div className="lg:col-span-5 space-y-4">
                <h3 className="text-xl font-bold text-slate-900">
                  Send money with clear confirmation
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Make peer-to-peer transfers using just an email address or wallet ID. If any part of the payment encounters an error, your money is returned automatically.
                </p>
                <div className="space-y-2.5 text-xs text-slate-600 pt-1">
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Transfers complete in under a second</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Automatic retry protection prevents duplicate debits</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Instant refund if the recipient account cannot be credited</span>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-7 bg-slate-50 border border-slate-200 rounded-lg p-5 space-y-3">
                <div className="flex items-center justify-between border-b border-slate-200/80 pb-3">
                  <span className="text-xs font-semibold text-slate-900">Transfer Confirmation</span>
                  <span className="text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 font-medium">
                    Delivered
                  </span>
                </div>
                <div className="bg-white border border-slate-200 p-4 rounded-lg space-y-2 text-xs">
                  <div className="flex justify-between py-1 border-b border-slate-100">
                    <span className="text-slate-500">Recipient</span>
                    <span className="font-semibold text-slate-900">bob.williams@payflow.demo</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-slate-100">
                    <span className="text-slate-500">Amount</span>
                    <span className="font-semibold text-slate-900">₹1,200.00</span>
                  </div>
                  <div className="flex justify-between py-1 border-b border-slate-100">
                    <span className="text-slate-500">Note</span>
                    <span className="text-slate-700">Dinner split</span>
                  </div>
                  <div className="flex justify-between py-1">
                    <span className="text-slate-500">Reference Number</span>
                    <span className="font-mono text-slate-600">TXN-8392A</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 3: Ledger History */}
          {activeTab === 'ledger' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center text-left">
              <div className="lg:col-span-5 space-y-4">
                <h3 className="text-xl font-bold text-slate-900">
                  Clear, balanced transaction history
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Every debit matches an equal credit. You can review your transaction history at any time with reference IDs, exact timestamps, and recipient details.
                </p>
                <div className="space-y-2.5 text-xs text-slate-600 pt-1">
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Permanent record for every deposit, withdrawal, and transfer</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Search and filter transactions by date or party</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Consistent double-entry accounting records</span>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-7 bg-slate-50 border border-slate-200 rounded-lg p-5 space-y-3">
                <div className="flex items-center justify-between border-b border-slate-200/80 pb-2.5">
                  <span className="text-xs font-semibold text-slate-900">Recent Activity</span>
                  <span className="text-xs text-slate-500">3 Transactions</span>
                </div>
                <div className="space-y-2">
                  <div className="bg-white border border-slate-200 rounded p-3 flex items-center justify-between text-xs">
                    <div>
                      <div className="font-semibold text-slate-900">Apex Commerce</div>
                      <div className="text-[11px] text-slate-500">Software subscription</div>
                    </div>
                    <div className="font-semibold text-slate-900">-₹1,200.00</div>
                  </div>
                  <div className="bg-white border border-slate-200 rounded p-3 flex items-center justify-between text-xs">
                    <div>
                      <div className="font-semibold text-slate-900">Wallet Top-up</div>
                      <div className="text-[11px] text-slate-500">Direct deposit</div>
                    </div>
                    <div className="font-semibold text-emerald-600">+₹5,000.00</div>
                  </div>
                  <div className="bg-white border border-slate-200 rounded p-3 flex items-center justify-between text-xs">
                    <div>
                      <div className="font-semibold text-slate-900">Rahul Sharma</div>
                      <div className="text-[11px] text-slate-500">Dinner split</div>
                    </div>
                    <div className="font-semibold text-slate-900">-₹450.00</div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 4: Safety & Protection */}
          {activeTab === 'security' && (
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center text-left">
              <div className="lg:col-span-5 space-y-4">
                <h3 className="text-xl font-bold text-slate-900">
                  Account security and fraud checks
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Every action is protected with secure session tokens, velocity monitoring, and automatic rejection of suspicious IP addresses.
                </p>
                <div className="space-y-2.5 text-xs text-slate-600 pt-1">
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>BCrypt password protection and token rotation</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Automated velocity checks on high-frequency transactions</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Check className="w-4 h-4 text-emerald-600 shrink-0" />
                    <span>Scoped API keys stored as secure SHA-256 hashes</span>
                  </div>
                </div>
              </div>

              <div className="lg:col-span-7 bg-slate-50 border border-slate-200 rounded-lg p-5 space-y-3">
                <div className="flex items-center justify-between border-b border-slate-200/80 pb-2.5">
                  <span className="text-xs font-semibold text-slate-900">Security Check Status</span>
                  <span className="text-[10px] font-mono text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 font-semibold">
                    Passed
                  </span>
                </div>
                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div className="p-3 bg-white border border-slate-200 rounded space-y-1">
                    <div className="text-slate-500 text-xs">Authentication</div>
                    <div className="font-semibold text-slate-900">JWT + Rotating Refresh</div>
                  </div>
                  <div className="p-3 bg-white border border-slate-200 rounded space-y-1">
                    <div className="text-slate-500 text-xs">API Key Format</div>
                    <div className="font-mono text-slate-900 text-[11px]">SHA-256 Hashed</div>
                  </div>
                </div>
                <div className="p-3 bg-white border border-slate-200 rounded text-xs text-slate-600">
                  Active session is signed and verified on every API request.
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
};
