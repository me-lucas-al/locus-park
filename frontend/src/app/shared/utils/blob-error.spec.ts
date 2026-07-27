import { HttpErrorResponse } from '@angular/common/http';
import { readBlobErrorMessage } from './blob-error';

describe('readBlobErrorMessage', () => {
  it('deve decodificar a mensagem de erro quando o corpo vem como Blob', async () => {
    const body = new Blob([JSON.stringify({ message: 'Periodo excede 20000 tickets.' })]);
    const error = new HttpErrorResponse({ status: 400, error: body });
    expect(await readBlobErrorMessage(error, 'fallback')).toBe('Periodo excede 20000 tickets.');
  });

  it('deve retornar mensagem de conexao quando status e 0', async () => {
    const error = new HttpErrorResponse({ status: 0 });
    expect(await readBlobErrorMessage(error, 'fallback')).toMatch(/conex/i);
  });

  it('deve retornar mensagem de permissao quando status e 403', async () => {
    const error = new HttpErrorResponse({ status: 403 });
    expect(await readBlobErrorMessage(error, 'fallback')).toMatch(/permiss/i);
  });

  it('deve retornar o fallback para erros que nao sao HttpErrorResponse', async () => {
    expect(await readBlobErrorMessage(new Error('boom'), 'fallback')).toBe('fallback');
  });
});
