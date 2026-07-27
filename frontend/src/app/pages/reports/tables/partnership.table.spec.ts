import { PARTNERSHIP_COLUMNS, toPartnershipRows } from './partnership.table';

describe('partnership.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toPartnershipRows([{ partnershipId: 'p-1', name: 'Shopping Central', usageCount: 5, discountGranted: 50 }]);
    const columnKeys = PARTNERSHIP_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });
});
