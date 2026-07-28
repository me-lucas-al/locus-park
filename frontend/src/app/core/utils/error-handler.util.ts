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
    // Caso o status seja 0 (servidor offline ou problema de conexão)
    if (err.status === 0) {
      return 'O servidor está iniciando ou indisponível. Por favor, aguarde alguns instantes e tente novamente.';
    }

    // Caso o status seja 500 ou superior (erro interno do servidor)
    if (err.status >= 500) {
      return 'Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.';
    }

    const errorBody = err.error;
    if (errorBody) {
      // Cenário 1: O corpo do erro já é um objeto parseado (Content-Type: JSON)
      if (typeof errorBody === 'object') {
        if ('message' in errorBody && typeof errorBody.message === 'string' && errorBody.message.trim()) {
          return errorBody.message;
        }
        if ('error' in errorBody && typeof errorBody.error === 'string' && errorBody.error.trim()) {
          return errorBody.error;
        }
      }

      // Cenário 2: O corpo do erro é uma string (comum em responseType: 'text')
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
          }
        } catch {
          // Se não for um JSON válido, mas for uma string de texto plano preenchida, retorna ela
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
