import { HttpErrorResponse } from '@angular/common/http';

/**
 * Utilitário modular para extrair mensagens de erro retornadas pelo backend.
 * Caso seja um erro de conexão (status 0) ou erro interno do servidor (status >= 500),
 * uma mensagem amigável e personalizada correspondente é retornada.
 */
export function getBackendErrorMessage(err: unknown, fallbackMessage: string): string {
  if (!err) {
    return fallbackMessage;
  }

  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'O servidor está iniciando ou indisponível. Por favor, aguarde alguns instantes e tente novamente.';
    }
    if (err.status >= 500) {
      return 'Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.';
    }
  }

  if (typeof err === 'object' && err !== null && 'error' in err) {
    const errorBody = (err as { error: unknown }).error;
    if (errorBody) {
      if (typeof errorBody === 'object') {
        const obj = errorBody as Record<string, unknown>;
        if ('message' in obj && typeof obj['message'] === 'string' && obj['message'].trim()) {
          return obj['message'];
        }
        if ('error' in obj && typeof obj['error'] === 'string' && obj['error'].trim()) {
          return obj['error'];
        }
        if ('detail' in obj && typeof obj['detail'] === 'string' && obj['detail'].trim()) {
          return obj['detail'];
        }
      }

      if (typeof errorBody === 'string') {
        try {
          const parsed = JSON.parse(errorBody);
          if (parsed && typeof parsed === 'object') {
            if ('message' in parsed && typeof parsed.message === 'string' && parsed.message.trim()) {
              return parsed.message;
            }
            if ('error' in parsed && typeof parsed.error === 'string' && parsed.error.trim()) {
              return parsed.error;
            }
            if ('detail' in parsed && typeof parsed.detail === 'string' && parsed.detail.trim()) {
              return parsed.detail;
            }
          }
        } catch {
          const trimmed = errorBody.trim();
          if (trimmed.length > 0 && !trimmed.startsWith('<!DOCTYPE') && trimmed.length < 200) {
            return trimmed;
          }
        }
      }
    }
  }

  return fallbackMessage;
}
