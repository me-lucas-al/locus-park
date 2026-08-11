import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { VehicleService } from './vehicle.service';
import { VehicleRequest, VehicleResponse } from './vehicle.types';
import { environment } from '@environments/environment';

const BASE = `${environment.apiUrl}vehicles`;

const mockRequest: VehicleRequest = { plate: 'ABC1234', model: 'Gol', color: 'Branco', type: 'CAR' };
const mockResponse: VehicleResponse = {
  id: 'v-1', plate: 'ABC1234', model: 'Gol', color: 'Branco', type: 'CAR', companyId: 'c-1',
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

  it('deve criar veículo via POST', async () => {
    const promise = firstValueFrom(service.create(mockRequest));
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve listar veículos via GET', async () => {
    const promise = firstValueFrom(service.listAll());
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockResponse]);
    expect(await promise).toEqual([mockResponse]);
  });

  it('deve buscar veículo por ID via GET', async () => {
    const promise = firstValueFrom(service.getById('v-1'));
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve atualizar veículo via PUT com id e payload corretos', async () => {
    const promise = firstValueFrom(service.update('v-1', mockRequest));
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockResponse);
    expect(await promise).toEqual(mockResponse);
  });

  it('deve excluir veículo via DELETE com id correto', async () => {
    let completed = false;
    service.delete('v-1').subscribe({ complete: () => { completed = true; } });
    const req = httpMock.expectOne(`${BASE}/v-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });
});
