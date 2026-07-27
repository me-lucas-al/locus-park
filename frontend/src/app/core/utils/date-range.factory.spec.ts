import { buildRange } from './date-range.factory';

describe('date-range.factory', () => {
  it('deve montar TODAY com from igual a to', () => {
    const range = buildRange('TODAY', new Date(2026, 6, 25));
    expect(range).toEqual({ from: '2026-07-25', to: '2026-07-25' });
  });

  it('deve montar LAST_7_DAYS com 7 dias corridos incluindo hoje', () => {
    const range = buildRange('LAST_7_DAYS', new Date(2026, 6, 25));
    expect(range).toEqual({ from: '2026-07-19', to: '2026-07-25' });
  });

  it('deve montar LAST_7_DAYS atravessando virada de mes e ano', () => {
    const range = buildRange('LAST_7_DAYS', new Date(2027, 0, 2));
    expect(range).toEqual({ from: '2026-12-27', to: '2027-01-02' });
  });

  it('deve montar THIS_MONTH do dia 1 ate hoje', () => {
    const range = buildRange('THIS_MONTH', new Date(2026, 6, 25));
    expect(range).toEqual({ from: '2026-07-01', to: '2026-07-25' });
  });

  it('deve montar THIS_MONTH com from igual a to quando hoje e dia 1', () => {
    const range = buildRange('THIS_MONTH', new Date(2026, 6, 1));
    expect(range).toEqual({ from: '2026-07-01', to: '2026-07-01' });
  });
});
