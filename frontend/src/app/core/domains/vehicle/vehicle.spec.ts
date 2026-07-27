import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { VehicleService } from './vehicle.service';
import { VehicleRequest, VehicleResponse } from './vehicle.types';
import { environment } from '@environments/environment';

const COMPANY_ID = 'c-1';
const BASE = `${environment.apiUrl}companies/${COMPANY_ID}/vehicles`;

const mockRequest: VehicleRequest = { plate: 'ABC1234', model: 'Gol', color: 'Branco', type: 'CAR' };
const mockResponse: VehicleResponse = {
  id: 'v-1', plate: 'ABC1234', model: 'Gol', color: 'Branco', type: 'CAR', companyId: COMPANY_ID,
};

describe('VehicleService', () => {
  let service: VehicleService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [VehicleService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(VehicleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve criar veículo via POST com companyId correto na URL', async () => {
    const promise = firstValueFrom(service.create(COMPANY_ID, mockRequest));
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve listar veículos da empresa via GET com companyId correto', async () => {
    const promise = firstValueFrom(service.listAll(COMPANY_ID));
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockResponse]);
    expect(await promise).toEqual([mockResponse]);
  });

  it('deve buscar veículo por ID via GET com companyId e id corretos', async () => {
    const promise = firstValueFrom(service.getById(COMPANY_ID, 'v-1'));
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve atualizar veículo via PUT com companyId, id e payload corretos', async () => {
    const promise = firstValueFrom(service.update(COMPANY_ID, 'v-1', mockRequest));
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve excluir veículo via DELETE com companyId e id corretos', async () => {
    let completed = false;
    service.delete(COMPANY_ID, 'v-1').subscribe({ complete: () => { completed = true; } });
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });
});
