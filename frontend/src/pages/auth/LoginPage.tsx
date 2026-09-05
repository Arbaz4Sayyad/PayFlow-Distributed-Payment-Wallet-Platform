import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, Lock, Mail, ArrowRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { login as loginApi } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { normalizeError } from '../../utils/errors';
import { loginSchema, LoginFormData } from '../../schemas/auth';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [isDemoLoading, setIsDemoLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    setServerError(null);
    try {
      const authData = await loginApi(data);
      login(authData);
      navigate(ROUTES.DASHBOARD);
    } catch (err) {
      const norm = normalizeError(err);
      setServerError(norm.message);
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
          <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
            {serverError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-md text-xs font-medium text-red-700 animate-in fade-in">
                {serverError}
              </div>
            )}

            <Input
              label="Work Email"
              type="email"
              required
              {...register('email')}
              error={errors.email?.message}
              placeholder="name@company.com"
              prefix={<Mail className="w-4 h-4 text-slate-400" />}
              autoComplete="email"
            />

            <div className="space-y-1">
              <Input
                label="Password"
                type={showPassword ? 'text' : 'password'}
                required
                {...register('password')}
                error={errors.password?.message}
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
              loading={isSubmitting}
              className="w-full mt-2"
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Sign In to PayFlow
            </Button>
          </form>

          {/* Quick 1-Click Demo Access */}
          <div className="mt-4 pt-4 border-t border-slate-100">
            <div className="bg-slate-50 border border-slate-200/80 rounded-lg p-3 text-center space-y-2">
              <Button
                type="button"
                variant="primary"
                size="sm"
                loading={isDemoLoading}
                className="w-full bg-slate-900 hover:bg-slate-800 text-white font-medium shadow-sm justify-center h-9"
                onClick={async () => {
                  setServerError(null);
                  setIsDemoLoading(true);
                  try {
                    const { demoLogin } = await import('../../api/demo');
                    const authData = await demoLogin();
                    login(authData);
                    navigate(ROUTES.DASHBOARD);
                  } catch (err) {
                    const norm = normalizeError(err);
                    setServerError(norm.message);
                  } finally {
                    setIsDemoLoading(false);
                  }
                }}
                rightIcon={<ArrowRight className="w-3.5 h-3.5 text-emerald-400" />}
              >
                Continue as Demo User (John Doe)
              </Button>
              <div className="text-[10px] text-slate-500 font-mono">
                demo@payflow.demo • Preloaded with ₹24,750.00
              </div>
            </div>
          </div>

          <div className="mt-4 text-center">
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
