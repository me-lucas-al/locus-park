import { HOURLY_COLUMNS, toHourlyRows } from './hourly.table';

describe('hourly.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toHourlyRows([{ hour: 8, entryCount: 3, exitCount: 1, revenue: 30 }]);
    const columnKeys = HOURLY_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });

  it('deve preencher a hora com zero a esquerda', () => {
    const rows = toHourlyRows([{ hour: 3, entryCount: 0, exitCount: 0, revenue: 0 }]);
    expect(rows[0].hour).toBe('03:00');
  });
});
