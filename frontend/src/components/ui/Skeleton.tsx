import React from 'react';

export interface SkeletonProps {
  className?: string;
  variant?: 'rectangular' | 'circular' | 'text';
}

export const Skeleton: React.FC<SkeletonProps> = ({
  className = 'h-4 w-full',
  variant = 'rectangular',
}) => {
  const variantStyles = {
    rectangular: 'rounded-md',
    circular: 'rounded-full',
    text: 'rounded-sm h-3 w-3/4 my-1',
  };

  return (
    <div
      aria-hidden="true"
      className={`animate-pulse bg-slate-200 ${variantStyles[variant]} ${className}`}
    />
  );
};
