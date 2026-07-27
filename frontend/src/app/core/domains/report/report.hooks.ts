import { inject, Signal } from '@angular/core';
import { injectQuery, keepPreviousData } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { ReportService } from './report.service';
import { DateRange } from '../../types/date-range.types';

export function isReportRangeEnabled(range: DateRange): boolean {
  if (!range.from || !range.to) return false;
  return range.from <= range.to;
}

export function useReportQuery(range: Signal<DateRange>, options?: { enabled?: boolean }) {
  const service = inject(ReportService);
  return injectQuery(() => ({
    queryKey: ['reports', 'summary', range().from, range().to] as const,
    queryFn: () => lastValueFrom(service.getReport(range())),
    enabled: (options?.enabled ?? true) && isReportRangeEnabled(range()),
    staleTime: 60_000,
    placeholderData: keepPreviousData,
  }));
}
