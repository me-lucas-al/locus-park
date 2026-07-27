import { HttpResponse } from '@angular/common/http';
import { filenameFromContentDisposition } from '../../../shared/utils/content-disposition';

export interface ReportExportResult {
  readonly blob: Blob;
  readonly filename: string;
}

export function toReportExportResult(response: HttpResponse<Blob>, fallback: string): ReportExportResult {
  if (!response.body) {
    throw new Error('Resposta de exportação vazia.');
  }
  const filename = filenameFromContentDisposition(response.headers.get('Content-Disposition'), fallback);
  return { blob: response.body, filename };
}
