import React from 'react';
import { Laptop } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { useToast } from '../../components/ui/Toast';

export const SecurityPage: React.FC = () => {
  const { success } = useToast();

  const handleRevokeSessions = () => {
    success('Sessions Terminated', 'All other active device tokens have been revoked.');
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Security & Keys</h1>
        <p className="text-xs text-slate-500 mt-0.5">Manage authentication credentials, tokens, and active sessions.</p>
      </div>

      <div className="space-y-4">
        {/* Password */}
        <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-subtle flex items-center justify-between">
          <div className="space-y-0.5">
            <h3 className="text-sm font-semibold text-slate-900">Account Password</h3>
            <p className="text-xs text-slate-500">Last changed 19 days ago. Standard SHA-512 with BCrypt hashing.</p>
          </div>
          <Button variant="secondary" size="sm">
            Change Password
          </Button>
        </div>

        {/* Sessions */}
        <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-subtle space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-900">Active Device Sessions</h3>
            <Button variant="secondary" size="sm" onClick={handleRevokeSessions}>
              Sign out other sessions
            </Button>
          </div>
          <div className="space-y-2 pt-2 text-xs">
            <div className="flex items-center justify-between p-2.5 bg-slate-50 border border-slate-100 rounded-md">
              <div className="flex items-center gap-2.5">
                <Laptop className="w-4 h-4 text-slate-600" />
                <div>
                  <p className="font-semibold text-slate-800">Chrome on Windows (Current Session)</p>
                  <p className="text-[11px] text-slate-400">IP: 127.0.0.1 • Localhost Gateway</p>
                </div>
              </div>
              <span className="text-[11px] font-medium text-emerald-600">Active Now</span>
            </div>
          </div>
        </div>

        {/* Two-Factor Authentication */}
        <div className="bg-white border border-slate-200 rounded-lg p-5 shadow-subtle flex items-center justify-between">
          <div className="space-y-0.5">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold text-slate-900">Hardware 2FA / TOTP</h3>
              <span className="text-[10px] uppercase font-mono bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded">
                Coming in v1.2
              </span>
            </div>
            <p className="text-xs text-slate-500">Hardware FIDO2 and Authenticator app validation.</p>
          </div>
          <Button variant="secondary" size="sm" disabled>
            Enable TOTP
          </Button>
        </div>
      </div>
    </div>
  );
};
