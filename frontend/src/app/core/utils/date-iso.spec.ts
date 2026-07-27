import { addDays, daysBetween, isIsoDate, toIsoDate } from './date-iso';

describe('date-iso', () => {
  it('deve converter Date para YYYY-MM-DD sem sofrer shift de fuso UTC', () => {
    expect(toIsoDate(new Date(2026, 6, 25, 23, 30))).toBe('2026-07-25');
  });

  it('deve preencher mes e dia com zero a esquerda', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05');
  });

  it('deve validar formato ISO de data', () => {
    expect(isIsoDate('2026-07-25')).toBe(true);
    expect(isIsoDate('25/07/2026')).toBe(false);
    expect(isIsoDate('')).toBe(false);
  });

  it('deve somar dias considerando virada de mes', () => {
    const result = addDays(new Date(2026, 6, 31), 1);
    expect(toIsoDate(result)).toBe('2026-08-01');
  });

  it('deve subtrair dias considerando virada de ano', () => {
    const result = addDays(new Date(2027, 0, 1), -1);
    expect(toIsoDate(result)).toBe('2026-12-31');
  });

  it('deve calcular a diferenca de dias entre duas datas ISO', () => {
    expect(daysBetween('2026-07-01', '2026-07-25')).toBe(24);
    expect(daysBetween('2026-07-25', '2026-07-25')).toBe(0);
  });

  it('deve calcular diferenca de dias atravessando ano bissexto', () => {
    expect(daysBetween('2028-02-28', '2028-03-01')).toBe(2);
  });
});
