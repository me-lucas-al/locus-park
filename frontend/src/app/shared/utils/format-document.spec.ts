import { formatDocument } from './format-document';

describe('formatDocument', () => {
  it('deve mascarar CPF', () => {
    expect(formatDocument('12345678901')).toBe('123.456.789-01');
  });

  it('deve mascarar CNPJ', () => {
    expect(formatDocument('12345678000195')).toBe('12.345.678/0001-95');
  });

  it('deve retornar traco para valor nulo', () => {
    expect(formatDocument(null)).toBe('—');
  });

  it('deve retornar o valor cru quando nao reconhecer o tamanho do documento', () => {
    expect(formatDocument('123')).toBe('123');
  });
});
