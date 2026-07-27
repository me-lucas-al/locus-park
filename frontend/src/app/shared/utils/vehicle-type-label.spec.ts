import { vehicleTypeLabel } from './vehicle-type-label';

describe('vehicleTypeLabel', () => {
  it('deve traduzir cada tipo de veiculo para portugues, incluindo acento em Caminhão', () => {
    expect(vehicleTypeLabel('CAR')).toBe('Carro');
    expect(vehicleTypeLabel('MOTORCYCLE')).toBe('Moto');
    expect(vehicleTypeLabel('VAN')).toBe('Van');
    expect(vehicleTypeLabel('TRUCK')).toBe('Caminhão');
  });
});
