import { ReportColumn } from '../components/report-table/report-table.component';
import { PaymentMethodSummary } from '../../../core/domains/report/report.types';
import { paymentMethodLabel } from '../../../shared/utils/payment-method-label';

export interface PaymentMethodRow {
  readonly method: string;
  readonly ticketCount: number;
  readonly revenue: number;
  readonly sharePercent: number;
}

export const PAYMENT_METHOD_COLUMNS: readonly ReportColumn[] = [
  { key: 'method', label: 'Forma de Pagamento', format: 'text' },
  { key: 'ticketCount', label: 'Atendimentos', format: 'integer', align: 'right' },
  { key: 'revenue', label: 'Receita', format: 'currency', align: 'right' },
  { key: 'sharePercent', label: 'Participação', format: 'percent', align: 'right' },
];

export function toPaymentMethodRows(summaries: readonly PaymentMethodSummary[]): PaymentMethodRow[] {
  return summaries.map((s) => ({
    method: paymentMethodLabel(s.method),
    ticketCount: s.ticketCount,
    revenue: s.revenue,
    sharePercent: s.sharePercent,
  }));
}
