import { inject, Signal } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { ClientService } from './client.service';
import { ClientRequest, UpdateClientParams } from './client.types';

export function useClientsQuery() {
  const service = inject(ClientService);
  return injectQuery(() => ({
    queryKey: ['clients'] as const,
    queryFn: () => lastValueFrom(service.getAll()),
    staleTime: Infinity,
  }));
}

export function useClientByIdQuery(id: Signal<string>) {
  const service = inject(ClientService);
  return injectQuery(() => ({
    queryKey: ['clients', id()] as const,
    queryFn: () => lastValueFrom(service.getById(id())),
    enabled: !!id(),
    staleTime: Infinity,
  }));
}

export function useCreateClientMutation() {
  const service = inject(ClientService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: ClientRequest) => lastValueFrom(service.create(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  }));
}

export function useUpdateClientMutation() {
  const service = inject(ClientService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: UpdateClientParams) =>
      lastValueFrom(service.update(params.id, params.request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  }));
}

export function useDeleteClientMutation() {
  const service = inject(ClientService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (id: string) => lastValueFrom(service.delete(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['clients'] }),
  }));
}
