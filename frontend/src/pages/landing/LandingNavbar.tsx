import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Menu, X, ArrowRight } from 'lucide-react';
import { ROUTES } from '../../constants/routes';
import { useAuth } from '../../hooks/useAuth';
import { Button } from '../../components/ui/Button';

export const LandingNavbar: React.FC = () => {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const scrollTo = (id: string) => {
    setMobileMenuOpen(false);
    if (location.pathname !== '/') {
      navigate('/#' + id);
      return;
    }
    const element = document.getElementById(id);
    if (element) {
      const navHeight = 70;
      const elementPosition = element.getBoundingClientRect().top + window.pageYOffset;
      window.scrollTo({
        top: elementPosition - navHeight,
        behavior: 'smooth',
      });
    }
  };

  return (
    <header
      className={`sticky top-0 z-50 w-full transition-all duration-200 ${
        scrolled
          ? 'bg-white/90 backdrop-blur-md border-b border-slate-200/90 shadow-[0_1px_3px_rgba(0,0,0,0.03)]'
          : 'bg-white/70 backdrop-blur-sm border-b border-slate-200/60'
      }`}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Mark */}
          <div className="flex items-center gap-3">
            <Link to={ROUTES.LANDING} className="flex items-center gap-2.5 group focus:outline-none">
              <div className="w-8 h-8 rounded-lg bg-slate-900 flex items-center justify-center text-white shadow-sm ring-1 ring-slate-800/80 group-hover:scale-[1.03] group-hover:bg-slate-800 transition-all duration-200">
                <svg className="w-4 h-4 text-emerald-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                </svg>
              </div>
              <span className="font-bold text-slate-900 tracking-tight text-[17px] group-hover:text-slate-700 transition-colors">
                PayFlow
              </span>
            </Link>

            <span className="hidden sm:inline-flex items-center gap-1.5 font-mono text-[10px] font-semibold text-slate-600 bg-slate-100/90 border border-slate-200/80 px-2 py-0.5 rounded-full">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
              v1.0.0
            </span>
          </div>

          {/* Center Navigation Links — Swapped: API Reference BEFORE Architecture */}
          <nav className="hidden md:flex items-center gap-1.5 p-1 bg-slate-100/70 border border-slate-200/60 rounded-full text-xs font-medium text-slate-600">
            <button
              onClick={() => scrollTo('workbench')}
              className="px-3 py-1 rounded-full hover:text-slate-900 hover:bg-white/80 transition-all duration-150 cursor-pointer"
            >
              Capabilities
            </button>
            <button
              onClick={() => scrollTo('api')}
              className="px-3 py-1 rounded-full hover:text-slate-900 hover:bg-white/80 transition-all duration-150 cursor-pointer"
            >
              API Reference
            </button>
            <button
              onClick={() => scrollTo('architecture')}
              className="px-3 py-1 rounded-full hover:text-slate-900 hover:bg-white/80 transition-all duration-150 cursor-pointer"
            >
              Architecture
            </button>
            <button
              onClick={() => scrollTo('security')}
              className="px-3 py-1 rounded-full hover:text-slate-900 hover:bg-white/80 transition-all duration-150 cursor-pointer"
            >
              Security
            </button>
            <button
              onClick={() => scrollTo('faq')}
              className="px-3 py-1 rounded-full hover:text-slate-900 hover:bg-white/80 transition-all duration-150 cursor-pointer"
            >
              FAQ
            </button>
          </nav>

          {/* Right Action Buttons */}
          <div className="hidden md:flex items-center gap-2.5">
            {isAuthenticated ? (
              <Button
                variant="primary"
                size="sm"
                className="rounded-full px-4 shadow-sm"
                onClick={() => navigate(ROUTES.DASHBOARD)}
                rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
              >
                Open Dashboard
              </Button>
            ) : (
              <>
                <button
                  onClick={() => navigate(ROUTES.LOGIN)}
                  className="text-xs font-medium text-slate-600 hover:text-slate-900 px-3 py-1.5 rounded-full hover:bg-slate-100/80 transition-colors cursor-pointer"
                >
                  Log in
                </button>
                <Button
                  variant="primary"
                  size="sm"
                  className="rounded-full px-4 shadow-sm bg-slate-900 hover:bg-slate-800 text-white"
                  onClick={async () => {
                    try {
                      const { demoLogin } = await import('../../api/demo');
                      const authData = await demoLogin();
                      login(authData);
                      navigate(ROUTES.DASHBOARD);
                    } catch {
                      navigate(ROUTES.LOGIN);
                    }
                  }}
                  rightIcon={<ArrowRight className="w-3.5 h-3.5 text-emerald-400" />}
                >
                  Explore Demo
                </Button>
              </>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100/80 transition-colors focus:outline-none"
              aria-label="Toggle navigation menu"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-slate-200 bg-white/95 backdrop-blur-lg px-4 py-4 space-y-3 animate-in fade-in slide-in-from-top-2 duration-150">
          <div className="flex flex-col space-y-1">
            <button
              onClick={() => scrollTo('workbench')}
              className="text-left px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
            >
              Capabilities
            </button>
            <button
              onClick={() => scrollTo('api')}
              className="text-left px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
            >
              API Reference
            </button>
            <button
              onClick={() => scrollTo('architecture')}
              className="text-left px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
            >
              Architecture
            </button>
            <button
              onClick={() => scrollTo('security')}
              className="text-left px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
            >
              Security
            </button>
            <button
              onClick={() => scrollTo('faq')}
              className="text-left px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
            >
              FAQ
            </button>
          </div>
          <div className="border-t border-slate-100 pt-3 flex flex-col gap-2">
            {isAuthenticated ? (
              <Button
                variant="primary"
                className="w-full justify-center rounded-full"
                onClick={() => {
                  setMobileMenuOpen(false);
                  navigate(ROUTES.DASHBOARD);
                }}
              >
                Go to Dashboard
              </Button>
            ) : (
              <>
                <Button
                  variant="secondary"
                  className="w-full justify-center rounded-full"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    navigate(ROUTES.LOGIN);
                  }}
                >
                  Log in
                </Button>
                <Button
                  variant="primary"
                  className="w-full justify-center rounded-full"
                  onClick={() => {
                    setMobileMenuOpen(false);
                    navigate(ROUTES.REGISTER);
                  }}
                >
                  Create account
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};
