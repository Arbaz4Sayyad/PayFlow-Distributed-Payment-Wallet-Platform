import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Lock, Mail, ArrowRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { login as loginApi } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { normalizeError } from '../../utils/errors';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('developer@payflow.com');
  const [password, setPassword] = useState('Password123!');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setErrorMsg('Please enter both email and password.');
      return;
    }

    setErrorMsg(null);
    setLoading(true);

    try {
      const authData = await loginApi({ email, password });
      login(authData);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      const norm = normalizeError(err);
      setErrorMsg(norm.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 font-sans select-none">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center gap-2 mb-2">
          <img src="/favicon.svg" alt="PayFlow" className="w-8 h-8 rounded" />
          <span className="text-xl font-bold tracking-tight text-slate-900">PayFlow</span>
        </div>
        <h2 className="text-sm text-slate-500 font-normal">
          Secure payments, simplified.
        </h2>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-[380px]">
        <div className="bg-white py-7 px-6 border border-slate-200 rounded-lg shadow-subtle sm:px-8">
          <form className="space-y-4" onSubmit={handleSubmit}>
            {errorMsg && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-md text-xs font-medium text-red-700 animate-in fade-in">
                {errorMsg}
              </div>
            )}

            <Input
              label="Work Email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="name@company.com"
              prefix={<Mail className="w-4 h-4 text-slate-400" />}
              autoComplete="email"
            />

            <div className="space-y-1">
              <Input
                label="Password"
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••••••"
                prefix={<Lock className="w-4 h-4 text-slate-400" />}
                suffix={
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="text-slate-400 hover:text-slate-600 focus:outline-none"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                }
                autoComplete="current-password"
              />
              <div className="flex justify-end">
                <a href="#forgot" className="text-[11px] text-blue-600 hover:underline">
                  Forgot password?
                </a>
              </div>
            </div>

            <Button
              type="submit"
              loading={loading}
              className="w-full mt-2"
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Sign In to PayFlow
            </Button>
          </form>

          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Don't have an account?{' '}
              <Link to={ROUTES.REGISTER} className="text-blue-600 font-medium hover:underline">
                Create account
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
