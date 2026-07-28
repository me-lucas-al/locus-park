import { extractErrorMessage, getApiErrorMessage } from './error-message';

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

describe('getApiErrorMessage', () => {
  it('deve extrair a mensagem de um HttpErrorResponse com objeto no body', () => {
    const err = { error: { message: 'Já existe um veículo cadastrado com esta placa.' } };
    expect(getApiErrorMessage(err, 'fallback')).toBe('Já existe um veículo cadastrado com esta placa.');
  });

  it('deve extrair a mensagem de um HttpErrorResponse com JSON string no body', () => {
    const err = { error: '{"message":"Placa duplicada"}' };
    expect(getApiErrorMessage(err, 'fallback')).toBe('Placa duplicada');
  });

  it('deve ignorar mensagem nativa Http failure response e usar o fallback', () => {
    const err = { message: 'Http failure response for http://localhost:8080/vehicles: 400 Bad Request' };
    expect(getApiErrorMessage(err, 'Erro ao cadastrar')).toBe('Erro ao cadastrar');
  });
});

