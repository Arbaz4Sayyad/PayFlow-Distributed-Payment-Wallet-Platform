import React, { useState } from 'react';
import { ChevronDown } from 'lucide-react';

interface FAQItem {
  q: string;
  a: string;
}

export const FAQSection: React.FC = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs: FAQItem[] = [
    {
      q: 'What is PayFlow?',
      a: 'PayFlow is a digital wallet and money transfer platform. It allows users to hold INR balances, transfer money to other users or merchants, and maintain a clear, double-entry record of all transactions.',
    },
    {
      q: 'Can I add and withdraw money from my wallet?',
      a: 'Yes. You can top up your wallet balance directly in the app. You can also withdraw funds to an external account as long as you have sufficient available balance.',
    },
    {
      q: 'How does PayFlow prevent double-charging?',
      a: 'Every transfer uses an idempotency key. Before debiting your account, PayFlow checks both a fast cache and database constraints. If a request with the same key is repeated, PayFlow returns the original result without charging you twice.',
    },
    {
      q: 'What happens if a transfer fails halfway through?',
      a: 'PayFlow uses an automated refund mechanism. If your account is debited but the recipient cannot be credited due to an error, your funds are automatically credited back to your wallet.',
    },
    {
      q: 'How do I review past transactions?',
      a: 'All deposits, withdrawals, and transfers are recorded with exact dates, amounts, status badges, and reference numbers in your Transactions page.',
    },
  ];

  return (
    <section id="faq" className="py-16 md:py-24 bg-slate-50/50 border-b border-slate-200/70">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
            Frequently asked questions
          </h2>
          <p className="text-xs sm:text-sm text-slate-600 mt-2">
            Quick answers about how PayFlow works.
          </p>
        </div>

        <div className="space-y-2.5 text-left">
          {faqs.map((faq, index) => {
            const isOpen = openIndex === index;
            return (
              <div
                key={faq.q}
                className="bg-white border border-slate-200 rounded-lg overflow-hidden shadow-subtle"
              >
                <button
                  type="button"
                  onClick={() => setOpenIndex(isOpen ? null : index)}
                  className="w-full px-5 py-4 text-left flex items-center justify-between gap-4 hover:bg-slate-50/60 transition-colors cursor-pointer"
                  aria-expanded={isOpen}
                >
                  <span className="text-xs sm:text-sm font-semibold text-slate-900">{faq.q}</span>
                  <ChevronDown
                    className={`w-4 h-4 text-slate-400 transition-transform duration-200 shrink-0 ${
                      isOpen ? 'transform rotate-180 text-slate-800' : ''
                    }`}
                  />
                </button>
                {isOpen && (
                  <div className="px-5 pb-4 pt-1 text-xs text-slate-600 leading-relaxed border-t border-slate-100">
                    {faq.a}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
};
