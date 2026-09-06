import React, { useState } from 'react';
import { ShoppingCart } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Dialog } from '../../components/ui/Dialog';
import { useToast } from '../../components/ui/Toast';
import { useIdempotency } from '../../hooks/useIdempotency';
import { DEMO_CONFIG, getDemoBalance, setDemoBalance, addDemoTransaction } from '../../api/demo';
import { withdrawWallet } from '../../api/wallet';

export const PaymentsPage: React.FC = () => {
  const { success, error: toastError } = useToast();
  const { getIdempotencyKey, resetIdempotencyKey } = useIdempotency();

  // Merchant payment state
  const [merchant, setMerchant] = useState('Amazon Marketplace');
  const [amount, setAmount] = useState('');
  const [orderId, setOrderId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const num = parseFloat(amount);
    if (isNaN(num) || num <= 0) {
      toastError('Invalid Amount', 'Please enter a valid payment amount.');
      return;
    }
    if (!orderId.trim()) {
      toastError('Order Reference Required', 'Please provide a valid Merchant Order ID or Invoice Reference.');
      return;
    }
    setIsModalOpen(true);
  };

  const handleConfirmPayment = async () => {
    setIsSubmitting(true);
    const idempKey = getIdempotencyKey();
    const num = parseFloat(amount);
    const walletId = DEMO_CONFIG.primaryUser.walletId;
    const paymentId = `PAY-${Math.floor(100000 + Math.random() * 900000)}`;

    try {
      await withdrawWallet(
        walletId,
        {
          amount: num,
          currency: 'INR' as any,
          referenceId: orderId || `ORD-${Date.now()}`,
          description: `Merchant checkout to ${merchant}`,
        },
        idempKey
      );
    } catch {
      // Continue to local state persistence
    }

    // Persist demo state
    const currentBal = getDemoBalance();
    const newBal = Math.max(0, currentBal - num);
    setDemoBalance(newBal);

    addDemoTransaction({
      id: paymentId,
      transactionNumber: `TXN-${paymentId.slice(0, 8).toUpperCase()}`,
      senderWalletId: walletId,
      recipientWalletId: 'EXT-MERCHANT-' + merchant.toUpperCase().replace(/\s+/g, '-').slice(0, 10),
      senderName: 'Arbaz Sayyad',
      recipientName: merchant,
      amount: num,
      amountMinor: Math.round(num * 100),
      currency: 'INR',
      type: 'MERCHANT_PAYMENT',
      status: 'COMPLETED',
      description: `Payment to ${merchant} (Ref: ${orderId})`,
      referenceId: orderId,
    });

    setIsSubmitting(false);
    setIsModalOpen(false);
    resetIdempotencyKey();
    success('Payment Successful', `Paid ₹${num.toFixed(2)} to ${merchant}. Reference: ${orderId}`);

    setAmount('');
    setOrderId('');
    window.dispatchEvent(new CustomEvent('payflow:wallet-updated'));
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
              { value: 'Amazon Marketplace', label: 'Amazon Marketplace (E-Commerce & Retail)' },
              { value: 'Flipkart Internet', label: 'Flipkart Internet (Online Marketplace)' },
              { value: 'Zepto Express', label: 'Zepto Express (Quick Commerce & Groceries)' },
              { value: 'Blinkit Commerce', label: 'Blinkit Commerce (Instant Grocery Delivery)' },
              { value: 'Swiggy Food & Instamart', label: 'Swiggy Food & Instamart' },
              { value: 'Zomato Food Delivery', label: 'Zomato Food Delivery' },
              { value: 'Uber Technologies', label: 'Uber Rides & Mobility' },
              { value: 'Ola Cabs', label: 'Ola Cabs (Transportation)' },
              { value: 'Netflix India', label: 'Netflix India (Digital Subscription)' },
              { value: 'Spotify Premium', label: 'Spotify Premium (Digital Media)' },
              { value: 'Apple Services', label: 'Apple Services (App Store & iCloud)' },
              { value: 'Google Cloud Platform', label: 'Google Cloud Platform (Infrastructure)' },
              { value: 'AWS Cloud Services', label: 'AWS Cloud Services (Enterprise Cloud)' },
              { value: 'Microsoft Azure', label: 'Microsoft Azure (Cloud Services)' },
              { value: 'Tata Power Utility', label: 'Tata Power / Electricity Bill' },
              { value: 'Airtel Broadband', label: 'Airtel / Jio Fiber (Broadband & Telecom)' },
            ]}
          />

          <Input
            label="Merchant Order ID / Reference"
            type="text"
            required
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            placeholder="e.g. ORD-98214"
          />

          <Input
            label="Amount (INR)"
            type="number"
            step="0.01"
            min="1"
            required
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="0.00"
            prefix={<span className="text-slate-500 font-medium">₹</span>}
          />

          <Button
            type="submit"
            variant="primary"
            className="w-full mt-2 bg-slate-900 hover:bg-slate-800 text-white font-medium"
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
              className="bg-slate-900 hover:bg-slate-800 text-white"
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
