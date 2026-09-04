import React from 'react';
import { Link } from 'react-router-dom';
import { ROUTES } from '../../constants/routes';

export const LandingFooter: React.FC = () => {
  const scrollTo = (id: string) => {
    const el = document.getElementById(id);
    if (el) {
      const navHeight = 70;
      const elementPosition = el.getBoundingClientRect().top + window.pageYOffset;
      window.scrollTo({
        top: elementPosition - navHeight,
        behavior: 'smooth',
      });
    }
  };

  return (
    <footer className="bg-slate-50 border-t border-slate-200/80 py-10 text-xs text-slate-500">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 pb-6 border-b border-slate-200/80">
          <div className="space-y-1 text-left">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-md bg-slate-900 flex items-center justify-center text-white">
                <svg className="w-3.5 h-3.5 text-emerald-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                </svg>
              </div>
              <span className="font-bold text-slate-900 tracking-tight text-sm">PayFlow</span>
            </div>
            <p className="text-slate-500 text-xs">
              Digital wallet and instant payment platform.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-6 text-xs font-medium text-slate-600">
            <button
              onClick={() => scrollTo('workbench')}
              className="hover:text-slate-900 transition-colors cursor-pointer"
            >
              Capabilities
            </button>
            <button
              onClick={() => scrollTo('api')}
              className="hover:text-slate-900 transition-colors cursor-pointer"
            >
              API Reference
            </button>
            <button
              onClick={() => scrollTo('architecture')}
              className="hover:text-slate-900 transition-colors cursor-pointer"
            >
              Architecture
            </button>
            <button
              onClick={() => scrollTo('security')}
              className="hover:text-slate-900 transition-colors cursor-pointer"
            >
              Security
            </button>
            <button
              onClick={() => scrollTo('faq')}
              className="hover:text-slate-900 transition-colors cursor-pointer"
            >
              FAQ
            </button>
            <Link to={ROUTES.LOGIN} className="hover:text-slate-900 transition-colors">
              Log in
            </Link>
            <Link to={ROUTES.REGISTER} className="hover:text-slate-900 transition-colors">
              Create account
            </Link>
          </div>
        </div>

        <div className="pt-5 flex flex-col sm:flex-row items-center justify-between gap-2 text-[11px] text-slate-400">
          <p>© 2026 PayFlow. All rights reserved.</p>
          <div className="text-slate-400">
            Built with Spring Boot, PostgreSQL, Kafka, and Redis
          </div>
        </div>
      </div>
    </footer>
  );
};
