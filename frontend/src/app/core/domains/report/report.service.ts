import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { DateRange } from '../../types/date-range.types';
import { ReportExportFormat, ReportResponse } from './report.types';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}reports`;

  getReport(range: DateRange): Observable<ReportResponse> {
    return this.http.get<ReportResponse>(this.baseUrl, {
      params: { from: range.from, to: range.to },
    });
  }

  downloadExport(format: ReportExportFormat, range: DateRange): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.baseUrl}/export`, {
      params: { format, from: range.from, to: range.to },
      responseType: 'blob',
      observe: 'response',
    });
  }
}
