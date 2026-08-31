import { z } from 'zod';

export const transferSchema = z.object({
  recipient: z.string().min(3, 'Recipient email or wallet ID is required'),
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((val) => {
      const num = parseFloat(val);
      return !isNaN(num) && num > 0;
    }, 'Amount must be greater than 0'),
  note: z.string().max(255, 'Note cannot exceed 255 characters').optional(),
});

export type TransferFormData = z.infer<typeof transferSchema>;
