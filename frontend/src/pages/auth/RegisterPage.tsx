import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Lock, Mail, Phone, User, Check, ArrowRight } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { ROUTES } from '../../constants/routes';
import { register as registerApi } from '../../api/auth';
import { useAuth } from '../../hooks/useAuth';
import { normalizeError } from '../../utils/errors';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
  });

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Password rules validation
  const hasLength = formData.password.length >= 8;
  const hasUpper = /[A-Z]/.test(formData.password);
  const hasLower = /[a-z]/.test(formData.password);
  const hasNumber = /[0-9]/.test(formData.password);
  const hasSpecial = /[^A-Za-z0-9]/.test(formData.password);
  const isPasswordValid = hasLength && hasUpper && hasLower && hasNumber && hasSpecial;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.email || !formData.phone || !formData.password) {
      setErrorMsg('Please fill in all required fields.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setErrorMsg('Passwords do not match.');
      return;
    }

    if (!isPasswordValid) {
      setErrorMsg('Please meet all password complexity requirements.');
      return;
    }

    setErrorMsg(null);
    setLoading(true);

    try {
      const authData = await registerApi({
        email: formData.email,
        phone: formData.phone,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
      });
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
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-10 sm:px-6 lg:px-8 font-sans select-none">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="inline-flex items-center justify-center gap-2 mb-2">
          <img src="/favicon.svg" alt="PayFlow" className="w-8 h-8 rounded" />
          <span className="text-xl font-bold tracking-tight text-slate-900">PayFlow</span>
        </div>
        <h2 className="text-sm text-slate-500 font-normal">
          Create an enterprise wallet account
        </h2>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-[440px]">
        <div className="bg-white py-7 px-6 border border-slate-200 rounded-lg shadow-subtle sm:px-8">
          <form className="space-y-3.5" onSubmit={handleSubmit}>
            {errorMsg && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-md text-xs font-medium text-red-700 animate-in fade-in">
                {errorMsg}
              </div>
            )}

            <div className="grid grid-cols-2 gap-3">
              <Input
                label="First Name"
                value={formData.firstName}
                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                placeholder="Arbaz"
                prefix={<User className="w-4 h-4 text-slate-400" />}
              />
              <Input
                label="Last Name"
                value={formData.lastName}
                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                placeholder="Sayyad"
              />
            </div>

            <Input
              label="Work Email"
              type="email"
              required
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="name@company.com"
              prefix={<Mail className="w-4 h-4 text-slate-400" />}
            />

            <Input
              label="Mobile Phone (E.164)"
              type="tel"
              required
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
              placeholder="+919876543210"
              description="Used for two-factor verification"
              prefix={<Phone className="w-4 h-4 text-slate-400" />}
            />

            <Input
              label="Password"
              type={showPassword ? 'text' : 'password'}
              required
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              placeholder="••••••••••••"
              prefix={<Lock className="w-4 h-4 text-slate-400" />}
              suffix={
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="text-slate-400 hover:text-slate-600 focus:outline-none"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              }
            />

            {/* Password Validation Checklist */}
            <div className="bg-slate-50 p-2.5 rounded border border-slate-200 text-[11px] space-y-1">
              <div className="flex items-center gap-1.5 text-slate-600">
                <span className={`w-3 h-3 rounded-full flex items-center justify-center ${hasLength ? 'bg-emerald-500 text-white' : 'bg-slate-300'}`}>
                  {hasLength && <Check className="w-2.5 h-2.5" />}
                </span>
                <span>Minimum 8 characters</span>
              </div>
              <div className="flex items-center gap-1.5 text-slate-600">
                <span className={`w-3 h-3 rounded-full flex items-center justify-center ${hasUpper && hasLower ? 'bg-emerald-500 text-white' : 'bg-slate-300'}`}>
                  {hasUpper && hasLower && <Check className="w-2.5 h-2.5" />}
                </span>
                <span>Uppercase & lowercase letters</span>
              </div>
              <div className="flex items-center gap-1.5 text-slate-600">
                <span className={`w-3 h-3 rounded-full flex items-center justify-center ${hasNumber && hasSpecial ? 'bg-emerald-500 text-white' : 'bg-slate-300'}`}>
                  {hasNumber && hasSpecial && <Check className="w-2.5 h-2.5" />}
                </span>
                <span>At least one number & special character</span>
              </div>
            </div>

            <Input
              label="Confirm Password"
              type={showPassword ? 'text' : 'password'}
              required
              value={formData.confirmPassword}
              onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
              placeholder="••••••••••••"
              prefix={<Lock className="w-4 h-4 text-slate-400" />}
            />

            <Button
              type="submit"
              loading={loading}
              className="w-full mt-2"
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Create Account
            </Button>
          </form>

          <div className="mt-5 pt-4 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Already have an account?{' '}
              <Link to={ROUTES.LOGIN} className="text-blue-600 font-medium hover:underline">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
