import { DateRange } from '../../types/date-range.types';
import { ReportExportFormat } from './report.types';

export function reportExportFallbackFilename(format: ReportExportFormat, range: DateRange): string {
  return `relatorio-locus-park-${range.from}-a-${range.to}.${format}`;
}
