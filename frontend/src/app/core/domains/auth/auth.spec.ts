import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';
import { LoginRequest, RegisterRequest, AuthResponse } from './auth.types';
import { environment } from '@environments/environment';

const BASE = `${environment.apiUrl}auth`;

const loginRequest: LoginRequest = { username: 'admin', password: 'senha123' };
const registerRequest: RegisterRequest = {
  name: 'Admin', username: 'admin', password: 'senha123', companyName: 'Estacionamento Central',
};
const authResponse: AuthResponse = { token: 'jwt.mock.token', id: 'u-1', username: 'admin', role: 'ADMIN', companyId: 'c-1' };

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve disparar POST para /auth/login e atualizar Signals com token e companyId', async () => {
    const promise = firstValueFrom(service.login(loginRequest));
    const req = httpMock.expectOne(`${BASE}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(authResponse);
    await promise;
    expect(service.token()).toBe('jwt.mock.token');
    expect(service.companyId()).toBe('c-1');
  });

  it('deve manter Signals nulos após erro 401 no login', async () => {
    let thrownError: unknown;
    const promise = firstValueFrom(service.login(loginRequest));
    const req = httpMock.expectOne(`${BASE}/login`);
    req.flush({ message: 'Credenciais inválidas' }, { status: 401, statusText: 'Unauthorized' });
    try { await promise; } catch (e) { thrownError = e; }
    expect(thrownError).toBeDefined();
    expect(service.token()).toBeNull();
    expect(service.companyId()).toBeNull();
  });

  it('deve disparar POST para /auth/register com payload correto', async () => {
    const promise = firstValueFrom(service.register(registerRequest));
    const req = httpMock.expectOne(`${BASE}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush('', { status: 201, statusText: 'Created' });
    expect(await promise).toBe('');
  });

  it('deve limpar Signals ao realizar logout', () => {
    service.token.set('jwt.mock.token');
    service.companyId.set('c-1');
    service.logout();
    expect(service.token()).toBeNull();
    expect(service.companyId()).toBeNull();
  });
});
