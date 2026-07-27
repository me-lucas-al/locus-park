import { PAYMENT_METHOD_COLUMNS, toPaymentMethodRows } from './payment-method.table';

describe('payment-method.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toPaymentMethodRows([{ method: 'PIX', ticketCount: 10, revenue: 100, sharePercent: 50 }]);
    const columnKeys = PAYMENT_METHOD_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });

  it('deve traduzir a forma de pagamento para portugues', () => {
    const rows = toPaymentMethodRows([{ method: 'DINHEIRO', ticketCount: 1, revenue: 1, sharePercent: 1 }]);
    expect(rows[0].method).toBe('Dinheiro');
  });
});
