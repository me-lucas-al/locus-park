import { validateRange } from './date-range.validation';

const NOW = new Date(2026, 6, 25);

describe('date-range.validation', () => {
  it('deve rejeitar data em formato invalido', () => {
    expect(validateRange({ from: '25/07/2026', to: '2026-07-25' }, NOW)).toEqual({
      valid: false,
      error: 'INVALID_DATE',
    });
  });

  it('deve rejeitar from posterior a to', () => {
    expect(validateRange({ from: '2026-07-25', to: '2026-07-01' }, NOW)).toEqual({
      valid: false,
      error: 'FROM_AFTER_TO',
    });
  });

  it('deve rejeitar data futura', () => {
    expect(validateRange({ from: '2026-07-01', to: '2026-08-01' }, NOW)).toEqual({
      valid: false,
      error: 'FUTURE_DATE',
    });
  });

  it('deve aceitar um periodo de 366 dias corridos', () => {
    expect(validateRange({ from: '2025-07-25', to: '2026-07-25' }, NOW)).toEqual({ valid: true });
  });

  it('deve rejeitar um periodo de 367 dias corridos', () => {
    expect(validateRange({ from: '2025-07-24', to: '2026-07-25' }, NOW)).toEqual({
      valid: false,
      error: 'RANGE_TOO_LONG',
    });
  });

  it('nunca deve instanciar Date dentro da validacao pura', () => {
    const frozenNow = new Date(2026, 6, 25);
    const result = validateRange({ from: '2026-07-01', to: '2026-07-25' }, frozenNow);
    expect(result.valid).toBe(true);
  });
});
