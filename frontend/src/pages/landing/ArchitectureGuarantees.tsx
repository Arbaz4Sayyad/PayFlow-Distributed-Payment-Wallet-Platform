import React from 'react';
import { Database, ShieldCheck, RefreshCw, Cpu } from 'lucide-react';

export const ArchitectureGuarantees: React.FC = () => {
  const guarantees = [
    {
      icon: Database,
      title: 'Accurate to the cent',
      desc: 'All amounts are calculated as integer minor units (paise/cents) to eliminate fractional rounding drift.',
    },
    {
      icon: ShieldCheck,
      title: 'No double-charging',
      desc: 'Transfers require unique keys that are checked across distributed cache and database constraints before debiting.',
    },
    {
      icon: RefreshCw,
      title: 'Automatic refunds',
      desc: 'If a transfer fails while crediting the recipient, funds return to the sender immediately without manual intervention.',
    },
    {
      icon: Cpu,
      title: 'Fault isolation',
      desc: 'Downstream services are protected with circuit breakers to prevent individual issues from affecting the whole platform.',
    },
  ];

  return (
    <section id="architecture" className="py-16 md:py-24 bg-slate-50/50 border-b border-slate-200/70">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
            Engineered for reliability.
          </h2>
          <p className="text-xs sm:text-sm text-slate-600 mt-2">
            Key architectural safeguards that keep balances consistent and transactions secure.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 text-left">
          {guarantees.map((item) => {
            const Icon = item.icon;
            return (
              <div
                key={item.title}
                className="p-5 rounded-xl border border-slate-200 bg-white shadow-subtle hover:shadow-card transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="w-8 h-8 rounded-lg bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-800 mb-3.5">
                    <Icon className="w-4 h-4" />
                  </div>
                  <h3 className="text-sm font-semibold text-slate-900 mb-1">{item.title}</h3>
                  <p className="text-xs text-slate-600 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
