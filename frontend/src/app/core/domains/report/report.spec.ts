import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { ReportService } from './report.service';
import { ReportResponse } from './report.types';
import { DateRange } from '../../types/date-range.types';
import { environment } from '@environments/environment';

const BASE = `${environment.apiUrl}reports`;
const RANGE: DateRange = { from: '2026-07-01', to: '2026-07-25' };

const mockReportResponse: ReportResponse = {
  period: { from: RANGE.from, to: RANGE.to, days: 25 },
  company: { id: 'c-1', name: 'Estacionamento Central', cnpj: '12.345.678/0001-90', totalSpots: 120 },
  summary: {
    revenue: {
      grossRevenue: 1500.5, discountGranted: 0, netRevenue: 1500.5, averageTicketValue: 12.5,
      highestTicketValue: 60, lowestTicketValue: 0, paidTicketCount: 120, freeExitCount: 0,
    },
    stay: { averageMinutes: 45, minimumMinutes: 5, maximumMinutes: 300, totalMinutes: 5400, openStayCount: 0 },
    occupancy: {
      totalSpots: 120, entryCount: 120, exitCount: 120, activeCount: 0, peakConcurrentVehicles: 80,
      peakAt: '2026-07-10T12:00:00', peakOccupancyRate: 0.66, averageOccupancyRate: 0.3, turnoverPerSpot: 1,
    },
  },
  paymentMethodSummaries: [],
  vehicleTypeSummaries: [],
  dailySummaries: [],
  hourlySummaries: [],
  partnershipSummaries: [],
  clientSummaries: [],
  tickets: [],
  ticketCount: 120,
  ticketsTruncated: false,
  totalRevenue: 1500.5,
  totalServices: 120,
  averageStayMinutes: 45,
};

describe('ReportService', () => {
  let service: ReportService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ReportService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve buscar o relatorio via GET com from/to e sem companyId (guarda anti-IDOR)', async () => {
    const promise = firstValueFrom(service.getReport(RANGE));
    const req = httpMock.expectOne((r) => r.url === BASE);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('from')).toBe(RANGE.from);
    expect(req.request.params.get('to')).toBe(RANGE.to);
    expect(req.request.params.has('companyId')).toBe(false);
    req.flush(mockReportResponse);
    expect(await promise).toEqual(mockReportResponse);
  });

  it('deve baixar a exportacao como blob com o formato e periodo corretos', async () => {
    const promise = firstValueFrom(service.downloadExport('pdf', RANGE));
    const req = httpMock.expectOne((r) => r.url === `${BASE}/export`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('format')).toBe('pdf');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF-']));
    await promise;
  });
});
