import { inject, Signal } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { PartnershipService } from './partnership.service';
import { PartnershipRequest, UpdatePartnershipParams } from './partnership.types';

export function usePartnershipsQuery() {
  const service = inject(PartnershipService);
  return injectQuery(() => ({
    queryKey: ['partnerships'] as const,
    queryFn: () => lastValueFrom(service.listAll()),
    staleTime: Infinity,
  }));
}

export function usePartnershipByIdQuery(id: Signal<string>) {
  const service = inject(PartnershipService);
  return injectQuery(() => ({
    queryKey: ['partnerships', id()] as const,
    queryFn: () => lastValueFrom(service.getById(id())),
    enabled: !!id(),
    staleTime: Infinity,
  }));
}

export function useCreatePartnershipMutation() {
  const service = inject(PartnershipService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: PartnershipRequest) => lastValueFrom(service.create(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partnerships'] }),
  }));
}

export function useUpdatePartnershipMutation() {
  const service = inject(PartnershipService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: UpdatePartnershipParams) =>
      lastValueFrom(service.update(params.id, params.request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partnerships'] }),
  }));
}

export function useDeletePartnershipMutation() {
  const service = inject(PartnershipService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (id: string) => lastValueFrom(service.delete(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['partnerships'] }),
  }));
}
