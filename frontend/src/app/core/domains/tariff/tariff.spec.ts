import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { TariffService } from './tariff.service';
import {
  TariffConfigurationRequest,
  TariffConfigurationResponse,
  PricingConfigurationRequest,
  PricingConfigurationResponse,
} from './tariff.types';
import { environment } from '@environments/environment';

const TARIFF_URL = `${environment.apiUrl}configurations/tariff`;
const PRICING_URL = `${environment.apiUrl}configurations/pricing`;

const mockTariffResponse: TariffConfigurationResponse = {
  id: 'tc-1', companyId: 'c-1', toleranceMinutes: 10,
  firstHourValue: 10, additionalFractionValue: 5, overnightFee: 50, lostTicketFee: 30,
};

const mockTariffRequest: TariffConfigurationRequest = {
  toleranceMinutes: 10,
  firstHourValue: 10,
  additionalFractionValue: 5,
  overnightFee: 50,
  lostTicketFee: 30,
};

const mockPricingResponse: PricingConfigurationResponse = {
  id: 'pc-1', companyId: 'c-1', dailyTriggerHours: 12, dailyValue: 80, monthlyBaseValue: 300,
};

const mockPricingRequest: PricingConfigurationRequest = {
  dailyTriggerHours: 12,
  dailyValue: 80,
  monthlyBaseValue: 300,
};

describe('TariffService', () => {
  let service: TariffService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TariffService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TariffService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve buscar configuração de tarifa via GET', async () => {
    const promise = firstValueFrom(service.getTariff());
    const req = httpMock.expectOne(TARIFF_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockTariffResponse);
    expect(await promise).toEqual(mockTariffResponse);
  });

  it('deve atualizar tarifa via PUT com payload correto', async () => {
    const promise = firstValueFrom(service.updateTariff(mockTariffRequest));
    const req = httpMock.expectOne(TARIFF_URL);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockTariffRequest);
    req.flush(mockTariffResponse);
    expect(await promise).toEqual(mockTariffResponse);
  });

  it('deve excluir tarifa via DELETE', async () => {
    let completed = false;
    service.deleteTariff().subscribe({ complete: () => { completed = true; } });
    const req = httpMock.expectOne(TARIFF_URL);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });

  it('deve buscar configuração de pricing via GET', async () => {
    const promise = firstValueFrom(service.getPricing());
    const req = httpMock.expectOne(PRICING_URL);
    expect(req.request.method).toBe('GET');
    req.flush(mockPricingResponse);
    expect(await promise).toEqual(mockPricingResponse);
  });

  it('deve atualizar pricing via PUT com payload correto', async () => {
    const promise = firstValueFrom(service.updatePricing(mockPricingRequest));
    const req = httpMock.expectOne(PRICING_URL);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockPricingRequest);
    req.flush(mockPricingResponse);
    expect(await promise).toEqual(mockPricingResponse);
  });

  it('deve excluir pricing via DELETE', async () => {
    let completed = false;
    service.deletePricing().subscribe({ complete: () => { completed = true; } });
    const req = httpMock.expectOne(PRICING_URL);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });
});
