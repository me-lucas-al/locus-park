import { VEHICLE_TYPE_COLUMNS, toVehicleTypeRows } from './vehicle-type.table';

describe('vehicle-type.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toVehicleTypeRows([{ type: 'TRUCK', ticketCount: 2, revenue: 200, sharePercent: 20 }]);
    const columnKeys = VEHICLE_TYPE_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });

  it('deve traduzir o tipo de veiculo, incluindo o acento em Caminhão', () => {
    const rows = toVehicleTypeRows([{ type: 'TRUCK', ticketCount: 1, revenue: 1, sharePercent: 1 }]);
    expect(rows[0].type).toBe('Caminhão');
  });
});
