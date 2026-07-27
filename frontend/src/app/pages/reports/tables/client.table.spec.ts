import { CLIENT_COLUMNS, toClientRows } from './client.table';

describe('client.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toClientRows([
      {
        clientId: 'c-1', name: 'Silva; Souza & Cia', cpf: '12345678901', ticketCount: 3,
        totalSpent: 90, averageStayMinutes: 60, paymentMethodsUsed: ['PIX', 'DINHEIRO'],
      },
    ]);
    const columnKeys = CLIENT_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });

  it('deve mascarar o CPF e juntar as formas de pagamento traduzidas', () => {
    const rows = toClientRows([
      { clientId: 'c-1', name: 'Ana', cpf: '12345678901', ticketCount: 1, averageStayMinutes: 30, totalSpent: 10, paymentMethodsUsed: ['PIX'] },
    ]);
    expect(rows[0].cpf).toBe('123.456.789-01');
    expect(rows[0].paymentMethodsUsed).toBe('PIX');
  });
});
