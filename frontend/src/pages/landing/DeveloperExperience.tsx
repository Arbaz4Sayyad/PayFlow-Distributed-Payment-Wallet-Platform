import React, { useState } from 'react';
import { Terminal, Copy, Check } from 'lucide-react';

export const DeveloperExperience: React.FC = () => {
  const [copied, setCopied] = useState(false);

  const curlCode = `curl -X POST https://api.payflow.com/api/v1/payments \\
  -H "Authorization: Bearer <TOKEN>" \\
  -H "Idempotency-Key: PAY-8921-AF7" \\
  -H "Content-Type: application/json" \\
  -d '{
    "senderWalletId": "1be4f2d0-31ea-4070-8dfd-d03af6f40c2e",
    "recipientWalletId": "2cf5e3e1-42fa-5181-9efd-e14bf7f51d3f",
    "amount": 1200.00,
    "currency": "INR",
    "paymentType": "P2P_TRANSFER",
    "description": "API Integration Settlement"
  }'`;

  const handleCopy = () => {
    navigator.clipboard.writeText(curlCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <section id="api" className="py-16 md:py-24 bg-white border-b border-slate-200/70">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center text-left">
          {/* Left Column: Developer Overview */}
          <div className="lg:col-span-5 space-y-4">
            <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900 leading-tight">
              Built for developers.
            </h2>
            <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
              Integrate payments and wallet operations with straightforward REST endpoints. Include an <code className="text-slate-800 font-mono bg-slate-100 px-1 py-0.5 rounded text-xs">Idempotency-Key</code> header to safely retry requests without risk of double-charging.
            </p>
            <div className="space-y-2 pt-2 text-xs text-slate-600">
              <div className="flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-slate-900" />
                <span>Simple JSON request and response format</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-slate-900" />
                <span>Full OpenAPI 3.0 documentation available</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="w-1.5 h-1.5 rounded-full bg-slate-900" />
                <span>Unique correlation IDs for end-to-end debugging</span>
              </div>
            </div>
          </div>

          {/* Right Column: Code Terminal Preview */}
          <div className="lg:col-span-7">
            <div className="bg-slate-900 rounded-xl border border-slate-800 shadow-xl overflow-hidden">
              {/* Terminal Titlebar */}
              <div className="bg-slate-950/80 px-4 py-3 border-b border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Terminal className="w-3.5 h-3.5 text-slate-400" />
                  <span className="text-xs font-mono text-slate-300">POST /api/v1/payments</span>
                </div>
                <button
                  onClick={handleCopy}
                  className="flex items-center gap-1 text-[11px] font-mono text-slate-400 hover:text-white transition-colors cursor-pointer"
                >
                  {copied ? (
                    <>
                      <Check className="w-3 h-3 text-emerald-400" />
                      <span className="text-emerald-400">Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-3 h-3" />
                      <span>Copy</span>
                    </>
                  )}
                </button>
              </div>

              {/* Code Snippet */}
              <div className="p-4 sm:p-5 overflow-x-auto">
                <pre className="font-mono text-xs text-slate-300 leading-relaxed">
                  <code>{curlCode}</code>
                </pre>
              </div>

              {/* Response Footer */}
              <div className="bg-slate-950/90 px-4 py-2.5 border-t border-slate-800 flex items-center justify-between font-mono text-[11px]">
                <span className="text-emerald-400 font-semibold">200 OK</span>
                <span className="text-slate-400">status: "SUCCESS"</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
