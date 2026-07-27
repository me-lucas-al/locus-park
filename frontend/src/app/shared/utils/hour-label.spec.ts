import { hourLabel } from './hour-label';

describe('hourLabel', () => {
  it('deve preencher a hora com zero a esquerda', () => {
    expect(hourLabel(3)).toBe('03:00');
  });

  it('deve formatar hora com dois digitos sem alteracao', () => {
    expect(hourLabel(23)).toBe('23:00');
  });
});
