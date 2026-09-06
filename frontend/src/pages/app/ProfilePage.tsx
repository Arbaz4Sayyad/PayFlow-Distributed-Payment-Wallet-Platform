import React, { useState, useEffect } from 'react';
import { CheckCircle2 } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../components/ui/Toast';

export const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const { success } = useToast();

  const [firstName, setFirstName] = useState(user?.firstName || 'Arbaz');
  const [lastName, setLastName] = useState(user?.lastName || 'Sayyad');
  const [phone, setPhone] = useState(user?.phone || '+15551234567');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (user) {
      if (user.firstName) setFirstName(user.firstName);
      if (user.lastName) setLastName(user.lastName);
      if (user.phone) setPhone(user.phone);
    }
  }, [user]);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setTimeout(() => {
      setIsSaving(false);
      success('Profile Updated', 'Your profile details have been saved.');
    }, 500);
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Account Settings</h1>
        <p className="text-xs text-slate-500 mt-0.5">Manage your identity and KYC tier level.</p>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle space-y-6">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-full bg-slate-900 text-white flex items-center justify-center text-base font-bold">
              {firstName.charAt(0)}
            </div>
            <div>
              <h2 className="text-sm font-semibold text-slate-900">{firstName} {lastName}</h2>
              <p className="text-xs text-slate-500">{user?.email || 'demo@payflow.demo'}</p>
            </div>
          </div>
          <Badge variant="success" size="md">
            <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
            KYC TIER 3 (VERIFIED)
          </Badge>
        </div>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="First Name"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
            />
            <Input
              label="Last Name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </div>

          <Input
            label="Email Address"
            type="email"
            value={user?.email || 'demo@payflow.demo'}
            disabled
            description="Email modifications require multi-factor authorization."
          />

          <Input
            label="Mobile Phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />

          <div className="pt-2 flex justify-end">
            <Button type="submit" variant="primary" loading={isSaving}>
              Save Changes
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
