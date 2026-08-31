import React from 'react';

export interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'prefix'> {
  label?: string;
  description?: string;
  error?: string;
  prefix?: React.ReactNode;
  suffix?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      description,
      error,
      prefix,
      suffix,
      className = '',
      id,
      required,
      disabled,
      ...props
    },
    ref
  ) => {
    const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

    return (
      <div className="w-full space-y-1.5">
        {label && (
          <div className="flex items-center justify-between">
            <label
              htmlFor={inputId}
              className="block text-xs font-medium text-slate-700"
            >
              {label} {required && <span className="text-red-500">*</span>}
            </label>
          </div>
        )}

        <div
          className={`relative flex items-center bg-white border rounded-md transition-colors shadow-subtle ${
            error
              ? 'border-red-500 focus-within:ring-2 focus-within:ring-red-500/20'
              : 'border-slate-200 hover:border-slate-300 focus-within:border-slate-900 focus-within:ring-1 focus-within:ring-slate-900'
          } ${disabled ? 'bg-slate-50 opacity-60 cursor-not-allowed' : ''}`}
        >
          {prefix && (
            <div className="pl-3 pr-2 flex items-center pointer-events-none text-slate-400 text-sm">
              {prefix}
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            disabled={disabled}
            aria-invalid={!!error}
            className={`w-full py-2 px-3 text-sm text-slate-900 placeholder:text-slate-400 bg-transparent focus:outline-none disabled:cursor-not-allowed ${
              prefix ? 'pl-0' : ''
            } ${suffix ? 'pr-0' : ''} ${className}`}
            {...props}
          />

          {suffix && (
            <div className="pr-3 pl-2 flex items-center text-slate-400 text-sm">
              {suffix}
            </div>
          )}
        </div>

        {description && !error && (
          <p className="text-xs text-slate-500">{description}</p>
        )}

        {error && (
          <p className="text-xs font-medium text-red-600 animate-in fade-in duration-150">
            {error}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
