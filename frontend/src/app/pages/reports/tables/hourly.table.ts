import { ReportColumn } from '../components/report-table/report-table.component';
import { HourlySummary } from '../../../core/domains/report/report.types';
import { hourLabel } from '../../../shared/utils/hour-label';

export interface HourlyRow {
  readonly hour: string;
  readonly entryCount: number;
  readonly exitCount: number;
  readonly revenue: number;
}

export const HOURLY_COLUMNS: readonly ReportColumn[] = [
  { key: 'hour', label: 'Hora (entrada)', format: 'text' },
  { key: 'entryCount', label: 'Entradas', format: 'integer', align: 'right' },
  { key: 'exitCount', label: 'Saídas', format: 'integer', align: 'right' },
  { key: 'revenue', label: 'Faturamento', format: 'currency', align: 'right' },
];

export function toHourlyRows(summaries: readonly HourlySummary[]): HourlyRow[] {
  return summaries.map((s) => ({
    hour: hourLabel(s.hour),
    entryCount: s.entryCount,
    exitCount: s.exitCount,
    revenue: s.revenue,
  }));
}
