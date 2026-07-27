import { HttpErrorResponse } from '@angular/common/http';
import { extractErrorMessage } from './error-message';

export async function readBlobErrorMessage(error: unknown, fallback: string): Promise<string> {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'Não foi possível conectar ao servidor. Verifique sua conexão.';
  if (error.status === 403) return 'Você não tem permissão para exportar relatórios.';
  if (error.error instanceof Blob) {
    const text = await error.error.text();
    return extractErrorMessage(text, fallback);
  }
  return fallback;
}
