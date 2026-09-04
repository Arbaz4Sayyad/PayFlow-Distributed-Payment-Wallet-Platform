import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';

export const FinalCTA: React.FC = () => {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();

  return (
    <section className="py-16 md:py-20 bg-white">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="p-8 sm:p-12 rounded-2xl bg-slate-900 text-white border border-slate-800 shadow-xl space-y-4">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-white leading-tight">
            Start moving money with PayFlow.
          </h2>
          <p className="text-xs sm:text-sm text-slate-300 max-w-md mx-auto leading-relaxed">
            Experience our live distributed payment architecture, double-entry ledger, and instant settlements right now with our interactive recruiter demo.
          </p>
          <div className="pt-2 flex flex-wrap items-center justify-center gap-3">
            <Button
              variant="secondary"
              size="md"
              className="bg-white text-slate-900 hover:bg-slate-100 rounded-full px-6 border-none font-medium shadow-sm"
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
              rightIcon={<ArrowRight className="w-4 h-4 text-emerald-600" />}
            >
              {isAuthenticated ? 'Open Dashboard' : 'Explore Demo (1-Click)'}
            </Button>
            {!isAuthenticated && (
              <Button
                variant="ghost"
                size="md"
                className="text-slate-300 hover:text-white hover:bg-slate-800 rounded-full px-4"
                onClick={() => navigate(ROUTES.REGISTER)}
              >
                Create free account
              </Button>
            )}
          </div>
        </div>
      </div>
    </section>
  );
};
