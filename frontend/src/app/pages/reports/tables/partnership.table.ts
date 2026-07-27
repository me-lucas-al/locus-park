import { ReportColumn } from '../components/report-table/report-table.component';
import { PartnershipSummary } from '../../../core/domains/report/report.types';

export interface PartnershipRow {
  readonly name: string;
  readonly usageCount: number;
  readonly discountGranted: number;
}

export const PARTNERSHIP_COLUMNS: readonly ReportColumn[] = [
  { key: 'name', label: 'Convênio', format: 'text' },
  { key: 'usageCount', label: 'Utilizações', format: 'integer', align: 'right' },
  { key: 'discountGranted', label: 'Desconto Concedido', format: 'currency', align: 'right' },
];

export function toPartnershipRows(summaries: readonly PartnershipSummary[]): PartnershipRow[] {
  return summaries.map((s) => ({
    name: s.name,
    usageCount: s.usageCount,
    discountGranted: s.discountGranted,
  }));
}
