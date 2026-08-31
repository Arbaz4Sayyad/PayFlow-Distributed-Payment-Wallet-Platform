import React, { useState } from 'react';
import { ShoppingCart } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Dialog } from '../../components/ui/Dialog';
import { useToast } from '../../components/ui/Toast';
import { useIdempotency } from '../../hooks/useIdempotency';

export const PaymentsPage: React.FC = () => {
  const { success, error: toastError } = useToast();
  const { getIdempotencyKey, resetIdempotencyKey } = useIdempotency();

  // Merchant payment state
  const [merchant, setMerchant] = useState('Amazon Marketplace');
  const [amount, setAmount] = useState('2450.00');
  const [orderId, setOrderId] = useState('ORD-AMZN-88319');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(amount);
    if (isNaN(num) || num <= 0) {
      toastError('Invalid Amount', 'Please enter a valid amount.');
      return;
    }
    setIsModalOpen(true);
  };

  const handleConfirmPayment = () => {
    setIsSubmitting(true);
    getIdempotencyKey();

    setTimeout(() => {
      setIsSubmitting(false);
      setIsModalOpen(false);
      resetIdempotencyKey();
      success('Payment Successful', `Paid ₹${amount} to ${merchant}. Order: ${orderId}`);
    }, 700);
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div className="border-b border-slate-200 pb-4">
        <h1 className="text-xl font-semibold text-slate-900 tracking-tight">Merchant Payments</h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Process payments to registered merchant gateways and manage checkout settlements.
        </p>
      </div>

      <div className="bg-white border border-slate-200 rounded-lg p-6 shadow-subtle">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select
            label="Merchant Entity"
            value={merchant}
            onChange={(e) => setMerchant(e.target.value)}
            options={[
              { value: 'Amazon Marketplace', label: 'Amazon Marketplace (E-Commerce)' },
              { value: 'Zepto Express', label: 'Zepto Express (Quick Commerce)' },
              { value: 'Uber Technologies', label: 'Uber Rides & Mobility' },
              { value: 'Zomato Food Delivery', label: 'Zomato Food Delivery' },
            ]}
          />

          <Input
            label="Merchant Order ID / Reference"
            type="text"
            required
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="ORD-12345"
          />

          <Input
            label="Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            prefix={<span className="text-slate-500 font-medium">₹</span>}
          />

          <Button
            type="submit"
            variant="primary"
            className="w-full mt-2"
            rightIcon={<ShoppingCart className="w-4 h-4" />}
          >
            Review & Authorize Payment
          </Button>
        </form>
      </div>

      {/* Confirmation Dialog */}
      <Dialog
        isOpen={isModalOpen}
        onClose={() => !isSubmitting && setIsModalOpen(false)}
        title="Authorize Merchant Checkout"
        description="Verify merchant billing details before approving debit."
        footer={
          <>
            <Button
              variant="secondary"
              size="sm"
              disabled={isSubmitting}
              onClick={() => setIsModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              loading={isSubmitting}
              disabled={isSubmitting}
              onClick={handleConfirmPayment}
            >
              Authorize Payment
            </Button>
          </>
        }
      >
        <div className="space-y-3 text-xs">
          <div className="p-3 bg-slate-50 border border-slate-200 rounded-md space-y-2">
            <div className="flex justify-between">
              <span className="text-slate-500">Merchant:</span>
              <span className="font-semibold text-slate-900">{merchant}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Order ID:</span>
              <span className="font-mono text-slate-900">{orderId}</span>
            </div>
            <div className="border-t border-slate-200 pt-2 flex justify-between text-sm font-bold text-slate-900">
              <span>Total Payable:</span>
              <span>₹{parseFloat(amount || '0').toFixed(2)} INR</span>
            </div>
          </div>
        </div>
      </Dialog>
    </div>
  );
};
