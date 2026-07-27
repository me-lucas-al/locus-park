import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { toReportExportResult } from './report-export.mapper';

describe('toReportExportResult', () => {
  it('deve extrair blob e nome de arquivo do Content-Disposition', () => {
    const blob = new Blob(['%PDF-']);
    const response = new HttpResponse({
      body: blob,
      headers: new HttpHeaders({ 'Content-Disposition': 'attachment; filename="relatorio.pdf"' }),
    });
    const result = toReportExportResult(response, 'fallback.pdf');
    expect(result.blob).toBe(blob);
    expect(result.filename).toBe('relatorio.pdf');
  });

  it('deve usar o fallback quando o header esta ausente', () => {
    const blob = new Blob(['%PDF-']);
    const response = new HttpResponse({ body: blob, headers: new HttpHeaders() });
    expect(toReportExportResult(response, 'fallback.pdf').filename).toBe('fallback.pdf');
  });

  it('deve lancar erro quando o corpo da resposta e nulo', () => {
    const response = new HttpResponse<Blob>({ body: null as unknown as Blob, headers: new HttpHeaders() });
    expect(() => toReportExportResult(response, 'fallback.pdf')).toThrow();
  });
});
