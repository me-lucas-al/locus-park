import { ReportColumn } from '../components/report-table/report-table.component';
import { DailySummary } from '../../../core/domains/report/report.types';

export interface DailyRow {
  readonly date: string;
  readonly entryCount: number;
  readonly exitCount: number;
  readonly revenue: number;
  readonly discount: number;
}

export const DAILY_COLUMNS: readonly ReportColumn[] = [
  { key: 'date', label: 'Data', format: 'date' },
  { key: 'entryCount', label: 'Entradas (entrada)', format: 'integer', align: 'right' },
  { key: 'exitCount', label: 'Saídas (saída)', format: 'integer', align: 'right' },
  { key: 'revenue', label: 'Faturamento (saída)', format: 'currency', align: 'right' },
  { key: 'discount', label: 'Desconto (saída)', format: 'currency', align: 'right' },
];

export function toDailyRows(summaries: readonly DailySummary[]): DailyRow[] {
  return summaries.map((s) => ({
    date: s.date,
    entryCount: s.entryCount,
    exitCount: s.exitCount,
    revenue: s.revenue,
    discount: s.discount,
  }));
}
