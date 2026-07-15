import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../domains/auth/auth.service';
import { isTokenExpired } from '../utils/jwt';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  const router = inject(Router);
  const authService = inject(AuthService);

  const isAuthRoute = req.url.includes('/auth/login') || 
                      req.url.includes('/auth/register') || 
                      req.url.includes('/login');

  if (token && !isAuthRoute) {
    if (isTokenExpired(token)) {
      authService.logout();
      router.navigate(['/login']);
      return throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Sessão expirada' }));
    }

    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthRoute) {
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};

