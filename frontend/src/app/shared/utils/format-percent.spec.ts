import { formatPercent } from './format-percent';

describe('formatPercent', () => {
  it('deve formatar percentual com decimal em virgula', () => {
    expect(formatPercent(28)).toBe('28,0%');
  });

  it('deve respeitar a quantidade de casas decimais informada', () => {
    expect(formatPercent(33.333, 2)).toBe('33,33%');
  });
});
