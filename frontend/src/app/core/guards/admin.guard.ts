import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isAdmin } from '../utils/jwt';
import { ToastService } from '../../shared/services/toast.service';

export const adminGuard: CanActivateFn = () => {
  if (isAdmin()) return true;

  const router = inject(Router);
  const toastService = inject(ToastService);
  toastService.error('Você não tem permissão para acessar esta página.');
  router.navigate(['/dashboard']);
  return false;
};
