import { inject } from '@angular/core';
import { injectMutation } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { ReportService } from './report.service';
import { toReportExportResult } from './report-export.mapper';
import { reportExportFallbackFilename } from './report-export.filename';
import { downloadBlob } from '../../../shared/utils/download-blob';
import { DateRange } from '../../types/date-range.types';
import { ReportExportFormat } from './report.types';

export interface ReportExportParams {
  readonly format: ReportExportFormat;
  readonly range: DateRange;
}

export function useReportExportMutation() {
  const service = inject(ReportService);
  return injectMutation(() => ({
    mutationFn: async (params: ReportExportParams) => {
      const response = await lastValueFrom(service.downloadExport(params.format, params.range));
      const fallback = reportExportFallbackFilename(params.format, params.range);
      const result = toReportExportResult(response, fallback);
      downloadBlob(result.blob, result.filename);
      return result;
    },
  }));
}
