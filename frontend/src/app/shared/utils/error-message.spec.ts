import { extractErrorMessage } from './error-message';

describe('extractErrorMessage', () => {
  it('deve extrair o campo message do corpo JSON', () => {
    expect(extractErrorMessage('{"message":"Periodo invalido."}', 'fallback')).toBe('Periodo invalido.');
  });

  it('deve usar error quando message estiver ausente', () => {
    expect(extractErrorMessage('{"error":"Erro generico"}', 'fallback')).toBe('Erro generico');
  });

  it('deve usar detail como ultimo recurso', () => {
    expect(extractErrorMessage('{"detail":"Detalhe do erro"}', 'fallback')).toBe('Detalhe do erro');
  });

  it('deve retornar o fallback quando o corpo nao e JSON valido', () => {
    expect(extractErrorMessage('<html>erro</html>', 'fallback')).toBe('fallback');
  });

  it('deve retornar o fallback quando nenhum campo reconhecido esta presente', () => {
    expect(extractErrorMessage('{"foo":"bar"}', 'fallback')).toBe('fallback');
  });
});
