import { DAILY_COLUMNS, toDailyRows } from './daily.table';

describe('daily.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toDailyRows([{ date: '2026-07-01', entryCount: 5, exitCount: 4, revenue: 100, discount: 10 }]);
    const columnKeys = DAILY_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });
});
