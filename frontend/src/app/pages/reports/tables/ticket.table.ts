import { ReportColumn } from '../components/report-table/report-table.component';
import { TicketRow } from '../../../core/domains/report/report.types';
import { vehicleTypeLabel } from '../../../shared/utils/vehicle-type-label';
import { paymentMethodLabel } from '../../../shared/utils/payment-method-label';

export interface TicketTableRow {
  readonly plate: string;
  readonly vehicleType: string;
  readonly clientName: string;
  readonly enteredAt: string;
  readonly exitedAt: string | null;
  readonly stayMinutes: number | null;
  readonly partnershipName: string;
  readonly paymentMethod: string;
  readonly grossAmount: number | null;
  readonly discountAmount: number | null;
  readonly totalAmount: number | null;
}

export const TICKET_COLUMNS: readonly ReportColumn[] = [
  { key: 'plate', label: 'Placa', format: 'text' },
  { key: 'vehicleType', label: 'Tipo', format: 'text' },
  { key: 'clientName', label: 'Cliente', format: 'text' },
  { key: 'enteredAt', label: 'Entrada', format: 'datetime' },
  { key: 'exitedAt', label: 'Saída', format: 'datetime' },
  { key: 'stayMinutes', label: 'Permanência', format: 'duration', align: 'right' },
  { key: 'partnershipName', label: 'Convênio', format: 'text' },
  { key: 'paymentMethod', label: 'Pagamento', format: 'text' },
  { key: 'grossAmount', label: 'Bruto', format: 'currency', align: 'right' },
  { key: 'discountAmount', label: 'Desconto', format: 'currency', align: 'right' },
  { key: 'totalAmount', label: 'Total', format: 'currency', align: 'right' },
];

export function toTicketRows(tickets: readonly TicketRow[]): TicketTableRow[] {
  return tickets.map((t) => ({
    plate: t.plate,
    vehicleType: vehicleTypeLabel(t.vehicleType),
    clientName: t.clientName ?? '—',
    enteredAt: t.enteredAt,
    exitedAt: t.exitedAt,
    stayMinutes: t.stayMinutes,
    partnershipName: t.partnershipName ?? '—',
    paymentMethod: t.paymentMethod ? paymentMethodLabel(t.paymentMethod) : '—',
    grossAmount: t.grossAmount,
    discountAmount: t.discountAmount,
    totalAmount: t.totalAmount,
  }));
}
