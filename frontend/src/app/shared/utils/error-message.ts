import { getBackendErrorMessage } from '../../core/utils/error-handler.util';

export function extractErrorMessage(body: string, fallback: string): string {
  try {
    const parsed = JSON.parse(body);
    const message = parsed?.message || parsed?.error || parsed?.detail;
    return typeof message === 'string' && message.trim() ? message : fallback;
  } catch {
    return fallback;
  }
}

export function getApiErrorMessage(err: unknown, fallback: string): string {
  return getBackendErrorMessage(err, fallback);
}


