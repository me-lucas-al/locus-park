import { formatDurationMinutes } from './format-duration';

describe('formatDurationMinutes', () => {
  it('deve formatar minutos menores que uma hora', () => {
    expect(formatDurationMinutes(45)).toBe('45m');
  });

  it('deve formatar horas e minutos', () => {
    expect(formatDurationMinutes(125)).toBe('2h 5m');
  });

  it('deve retornar traco para valor nulo', () => {
    expect(formatDurationMinutes(null)).toBe('—');
  });

  it('deve retornar traco para valor negativo', () => {
    expect(formatDurationMinutes(-5)).toBe('—');
  });
});
