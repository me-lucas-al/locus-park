import { ReportColumn } from '../components/report-table/report-table.component';
import { ClientSummary } from '../../../core/domains/report/report.types';
import { formatDocument } from '../../../shared/utils/format-document';
import { paymentMethodLabel } from '../../../shared/utils/payment-method-label';

export interface ClientRow {
  readonly name: string;
  readonly cpf: string;
  readonly ticketCount: number;
  readonly totalSpent: number;
  readonly averageStayMinutes: number;
  readonly paymentMethodsUsed: string;
}

export const CLIENT_COLUMNS: readonly ReportColumn[] = [
  { key: 'name', label: 'Cliente', format: 'text' },
  { key: 'cpf', label: 'CPF', format: 'text' },
  { key: 'ticketCount', label: 'Atendimentos', format: 'integer', align: 'right' },
  { key: 'totalSpent', label: 'Total Gasto', format: 'currency', align: 'right' },
  { key: 'averageStayMinutes', label: 'Permanência Média', format: 'duration', align: 'right' },
  { key: 'paymentMethodsUsed', label: 'Formas de Pagamento', format: 'text' },
];

export function toClientRows(summaries: readonly ClientSummary[]): ClientRow[] {
  return summaries.map((s) => ({
    name: s.name,
    cpf: formatDocument(s.cpf),
    ticketCount: s.ticketCount,
    totalSpent: s.totalSpent,
    averageStayMinutes: s.averageStayMinutes,
    paymentMethodsUsed: s.paymentMethodsUsed.map(paymentMethodLabel).join(', '),
  }));
}
