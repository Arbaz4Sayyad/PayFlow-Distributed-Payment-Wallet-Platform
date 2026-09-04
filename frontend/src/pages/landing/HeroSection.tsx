import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';
import { HeroInteractiveWidget } from './HeroInteractiveWidget';

export const HeroSection: React.FC = () => {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();

  return (
    <section className="pt-12 pb-16 md:pt-16 md:pb-20 border-b border-slate-200/70 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-14 items-center">
          {/* Left Column: Direct, human product copy */}
          <div className="lg:col-span-6 space-y-5 text-left">
            <h1 className="text-3xl sm:text-4xl lg:text-[42px] font-bold tracking-tight text-slate-900 leading-[1.15]">
              Digital wallet and instant payments for modern teams.
            </h1>

            <p className="text-sm sm:text-base text-slate-600 leading-relaxed max-w-xl">
              PayFlow lets you hold balances, send money instantly, and track every transaction with built-in double-entry accounting. Simple to use, and built on rock-solid infrastructure.
            </p>

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <Button
                variant="primary"
                size="md"
                className="rounded-full px-6 shadow-sm bg-slate-900 hover:bg-slate-800 text-white"
                onClick={async () => {
                  if (isAuthenticated) {
                    navigate(ROUTES.DASHBOARD);
                  } else {
                    try {
                      const { demoLogin } = await import('../../api/demo');
                      const authData = await demoLogin();
                      login(authData);
                      navigate(ROUTES.DASHBOARD);
                    } catch {
                      navigate(ROUTES.LOGIN);
                    }
                  }
                }}
                rightIcon={<ArrowRight className="w-4 h-4 text-emerald-400" />}
              >
                {isAuthenticated ? 'Open Dashboard' : 'Explore Demo'}
              </Button>

              <button
                onClick={() => {
                  const el = document.getElementById('workbench');
                  if (el) el.scrollIntoView({ behavior: 'smooth' });
                }}
                className="text-xs font-medium text-slate-700 hover:text-slate-950 px-4 py-2.5 rounded-full border border-slate-200 hover:bg-slate-50 transition-colors cursor-pointer"
              >
                See how it works
              </button>
            </div>

            {/* Practical trust points */}
            <div className="pt-6 border-t border-slate-100 grid grid-cols-3 gap-4 text-left">
              <div>
                <div className="text-xs font-semibold text-slate-900">Instant settlements</div>
                <div className="text-[11px] text-slate-500 mt-0.5">Real-time P2P transfers</div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-900">Zero double-charges</div>
                <div className="text-[11px] text-slate-500 mt-0.5">Automatic idempotency</div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-900">Full audit trail</div>
                <div className="text-[11px] text-slate-500 mt-0.5">Balanced ledger entries</div>
              </div>
            </div>
          </div>

          {/* Right Column: Live Interactive Product Widget */}
          <div className="lg:col-span-6">
            <HeroInteractiveWidget />
          </div>
        </div>
      </div>
    </section>
  );
};
