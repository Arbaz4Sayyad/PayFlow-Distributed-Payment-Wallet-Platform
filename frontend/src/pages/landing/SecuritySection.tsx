import React from 'react';
import { ShieldCheck, KeyRound, Database, Activity } from 'lucide-react';

export const SecuritySection: React.FC = () => {
  const securityFeatures = [
    {
      icon: KeyRound,
      title: 'Session & Token Security',
      description: 'Signed access tokens with rotating refresh tokens ensure sessions stay secure without storing passwords in plain text.',
    },
    {
      icon: Database,
      title: 'Balanced Double-Entry Records',
      description: 'Every deposit, withdrawal, and transfer produces matching debit and credit records so the ledger always balances.',
    },
    {
      icon: Activity,
      title: 'Risk & Velocity Checks',
      description: 'High-frequency transaction monitoring and IP blacklists help block unauthorized or suspicious payment attempts.',
    },
    {
      icon: ShieldCheck,
      title: 'Hashed API Credentials',
      description: 'Merchant keys are shown only once at creation and stored exclusively as SHA-256 hashes.',
    },
  ];

  return (
    <section id="security" className="py-16 md:py-24 bg-white border-b border-slate-200/70">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
            Built with security in mind.
          </h2>
          <p className="text-xs sm:text-sm text-slate-600 mt-2">
            Practical security features implemented throughout authentication, sessions, and payment flows.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 text-left">
          {securityFeatures.map((item) => {
            const Icon = item.icon;
            return (
              <div
                key={item.title}
                className="p-5 rounded-xl border border-slate-200 bg-slate-50/40 hover:bg-slate-50 transition-colors"
              >
                <div className="w-8 h-8 rounded-lg bg-white border border-slate-200 flex items-center justify-center text-slate-800 mb-3.5 shadow-subtle">
                  <Icon className="w-4 h-4" />
                </div>
                <h3 className="text-sm font-semibold text-slate-900 mb-1">{item.title}</h3>
                <p className="text-xs text-slate-600 leading-relaxed">{item.description}</p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
