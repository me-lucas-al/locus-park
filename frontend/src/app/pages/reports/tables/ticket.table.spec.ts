import { TICKET_COLUMNS, toTicketRows } from './ticket.table';

const BASE_TICKET = {
  ticketId: 't-1', status: 'PAID' as const, plate: 'ABC1D23', model: 'Gol', color: 'Prata',
  vehicleType: 'CAR' as const, clientName: 'Ana', clientCpf: '12345678901',
  enteredAt: '2026-07-01T10:00:00', exitedAt: '2026-07-01T11:00:00', stayMinutes: 60,
  partnershipName: null, paymentMethod: 'PIX' as const, grossAmount: 10, discountAmount: 0, totalAmount: 10,
};

describe('ticket.table', () => {
  it('deve gerar linhas cujas chaves batem exatamente com as colunas', () => {
    const rows = toTicketRows([BASE_TICKET]);
    const columnKeys = TICKET_COLUMNS.map((c) => c.key).sort();
    expect(Object.keys(rows[0]).sort()).toEqual(columnKeys);
  });

  it('deve usar traco para cliente e convenio ausentes', () => {
    const rows = toTicketRows([{ ...BASE_TICKET, clientName: null, partnershipName: null }]);
    expect(rows[0].clientName).toBe('—');
    expect(rows[0].partnershipName).toBe('—');
  });
});
