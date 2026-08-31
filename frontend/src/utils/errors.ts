import axios, { AxiosError } from 'axios';
import { ApiResponse } from '../types';

export interface NormalizedError {
  status: number;
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
}

export function normalizeError(error: unknown): NormalizedError {
  if (axios.isAxiosError(error)) {
    const axiosErr = error as AxiosError<ApiResponse<unknown>>;
    const status = axiosErr.response?.status || 500;
    const serverErr = axiosErr.response?.data?.error;

    // Field-level validation errors
    const fieldErrors: Record<string, string> = {};
    if (serverErr?.validationErrors && Array.isArray(serverErr.validationErrors)) {
      serverErr.validationErrors.forEach((v) => {
        if (v.field && v.message) {
          fieldErrors[v.field] = v.message;
        }
      });
    }

    if (serverErr?.message) {
      return {
        status,
        code: serverErr.code || `ERR_${status}`,
        message: serverErr.message,
        fieldErrors: Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
      };
    }

    switch (status) {
      case 400:
        return { status, code: 'BAD_REQUEST', message: 'Invalid request parameters.' };
      case 401:
        return { status, code: 'UNAUTHORIZED', message: 'Your session has expired. Please sign in again.' };
      case 403:
        return { status, code: 'FORBIDDEN', message: 'You do not have permission to perform this action.' };
      case 404:
        return { status, code: 'NOT_FOUND', message: 'The requested resource was not found.' };
      case 409:
        return { status, code: 'CONFLICT', message: 'Request already processed or idempotency conflict.' };
      case 422:
        return { status, code: 'UNPROCESSABLE_ENTITY', message: 'Validation failed for request data.', fieldErrors };
      case 429:
        return { status, code: 'RATE_LIMITED', message: 'Too many requests. Please slow down and try again.' };
      case 503:
        return { status, code: 'SERVICE_UNAVAILABLE', message: 'Service temporarily unavailable. Please retry shortly.' };
      default:
        return { status, code: 'SERVER_ERROR', message: 'An unexpected system error occurred. Please try again.' };
    }
  }

  if (error instanceof Error) {
    return {
      status: 500,
      code: 'CLIENT_ERROR',
      message: error.message,
    };
  }

  return {
    status: 500,
    code: 'UNKNOWN_ERROR',
    message: 'An unexpected error occurred.',
  };
}
