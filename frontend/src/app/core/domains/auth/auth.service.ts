import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, retry, timer, throwError } from 'rxjs';
import { environment } from '@environments/environment';
import { LoginRequest, RegisterRequest, AuthResponse } from './auth.types';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}auth`;

  readonly token = signal<string | null>(localStorage.getItem('token'));
  readonly companyId = signal<string | null>(localStorage.getItem('companyId'));

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      retry({
        count: 2,
        delay: (error, retryCount) => {
          if (error.status === 0 || error.status >= 500) {
            console.warn(`Erro de conexão ou servidor (${error.status}). Tentando novamente em ${retryCount * 2}s (tentativa ${retryCount}/2)...`);
            return timer(retryCount * 2000);
          }
          return throwError(() => error);
        },
      }),
      tap((response) => {
        this.token.set(response.token);
        localStorage.setItem('token', response.token);
        if (response.companyId) {
          this.companyId.set(response.companyId);
          localStorage.setItem('companyId', response.companyId);
        }
      })
    );
  }

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.baseUrl}/register`, request, { responseType: 'text' });
  }

  logout(): void {
    this.token.set(null);
    this.companyId.set(null);
    localStorage.removeItem('token');
    localStorage.removeItem('companyId');
  }
}
