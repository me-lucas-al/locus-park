import { inject, Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { CompanyService } from './company.service';

export function useCompanyQuery(companyId: Signal<string>) {
  const service = inject(CompanyService);
  return injectQuery(() => ({
    queryKey: ['company', companyId()] as const,
    queryFn: () => lastValueFrom(service.getById(companyId())),
    enabled: !!companyId() && companyId() !== 'null' && companyId() !== 'undefined',
    staleTime: Infinity,
  }));
}
