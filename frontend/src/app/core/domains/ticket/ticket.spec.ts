import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { TicketService } from './ticket.service';
import { TicketResponse } from './ticket.types';
import { environment } from '@environments/environment';

const BASE = `${environment.apiUrl}tickets`;

const mockVehicle = { id: 'v-1', plate: 'ABC1234', model: 'Gol', color: 'Branco', type: 'CAR' as const, companyId: 'c-1' };
const mockTicket: TicketResponse = {
  id: 't-1', companyId: 'c-1', vehicle: mockVehicle,
  enteredAt: '2024-01-01T10:00:00Z', status: 'ACTIVE',
};

describe('TicketService', () => {
  let service: TicketService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TicketService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TicketService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve disparar POST para check-in com vehicleId na query string', async () => {
    const promise = firstValueFrom(service.checkIn('v-1'));
    const req = httpMock.expectOne(`${BASE}/check-in?vehicleId=v-1`);
    expect(req.request.method).toBe('POST');
    req.flush(mockTicket);
    expect(await promise).toEqual(mockTicket);
  });

  it('deve propagar erro 400 no check-in', async () => {
    let thrownError: unknown;
    const promise = firstValueFrom(service.checkIn('v-1'));
    const req = httpMock.expectOne(`${BASE}/check-in?vehicleId=v-1`);
    req.flush({ message: 'Veículo já possui ticket ativo' }, { status: 400, statusText: 'Bad Request' });
    try { await promise; } catch (e) { thrownError = e; }
    expect(thrownError).toBeDefined();
  });

  it('deve disparar POST para check-out com paymentMethod', async () => {
    const exited = { ...mockTicket, exitedAt: '2024-01-01T12:00:00Z', status: 'CLOSED', paymentMethod: 'DINHEIRO' as const };
    const promise = firstValueFrom(service.checkOut('t-1', 'DINHEIRO'));
    const req = httpMock.expectOne(`${BASE}/t-1/check-out?paymentMethod=DINHEIRO`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ paymentMethod: 'DINHEIRO' });
    req.flush(exited);
    expect(await promise).toEqual(exited);
  });

  it('deve listar todos os tickets via GET', async () => {
    const promise = firstValueFrom(service.getAll());
    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockTicket]);
    expect(await promise).toEqual([mockTicket]);
  });

  it('deve buscar ticket por ID via GET', async () => {
    const promise = firstValueFrom(service.getById('t-1'));
    const req = httpMock.expectOne(`${BASE}/t-1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTicket);
    expect(await promise).toEqual(mockTicket);
  });

  it('deve deletar ticket via DELETE', async () => {
    let completed = false;
    service.delete('t-1').subscribe({ complete: () => { completed = true; } });
    const req = httpMock.expectOne(`${BASE}/t-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    expect(completed).toBe(true);
  });

  it('deve aplicar parceria via PATCH com partnershipId correto', async () => {
    const updated = { ...mockTicket, partnershipId: 'p-1' };
    const promise = firstValueFrom(service.applyPartnership('t-1', 'p-1'));
    const req = httpMock.expectOne(`${BASE}/t-1/partnership?partnershipId=p-1`);
    expect(req.request.method).toBe('PATCH');
    req.flush(updated);
    expect(await promise).toEqual(updated);
  });
});
