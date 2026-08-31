import { z } from 'zod';

export const topUpSchema = z.object({
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((val) => {
      const num = parseFloat(val);
      return !isNaN(num) && num >= 1;
    }, 'Minimum deposit is ₹1.00'),
  paymentMethod: z.string().min(1, 'Payment method is required'),
});

export type TopUpFormData = z.infer<typeof topUpSchema>;

export const withdrawSchema = z.object({
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((val) => {
      const num = parseFloat(val);
      return !isNaN(num) && num >= 1;
    }, 'Minimum withdrawal is ₹1.00'),
  bankAccount: z.string().min(1, 'Bank account is required'),
});

export type WithdrawFormData = z.infer<typeof withdrawSchema>;
