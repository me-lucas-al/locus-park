import { inject, Signal } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { VehicleService } from './vehicle.service';
import { VehicleRequest } from './vehicle.types';

export function useVehiclesQuery(companyId: Signal<string>) {
  const service = inject(VehicleService);
  return injectQuery(() => ({
    queryKey: ['vehicles', companyId()] as const,
    queryFn: () => lastValueFrom(service.listAll()),
    enabled: !!companyId() && companyId() !== 'null' && companyId() !== 'undefined',
  }));
}

export function useVehicleByIdQuery(companyId: Signal<string>, id: Signal<string>) {
  const service = inject(VehicleService);
  return injectQuery(() => ({
    queryKey: ['vehicles', companyId(), id()] as const,
    queryFn: () => lastValueFrom(service.getById(id())),
    enabled: !!companyId() && companyId() !== 'null' && companyId() !== 'undefined' && !!id(),
  }));
}

export function useCreateVehicleMutation(companyId: Signal<string>) {
  const service = inject(VehicleService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: VehicleRequest) => lastValueFrom(service.create(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vehicles', companyId()] }),
  }));
}

export function useUpdateVehicleMutation(companyId: Signal<string>) {
  const service = inject(VehicleService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: { id: string; request: VehicleRequest }) =>
      lastValueFrom(service.update(params.id, params.request)),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['vehicles', companyId()] }),
  }));
}

export function useDeleteVehicleMutation(companyId: Signal<string>) {
  const service = inject(VehicleService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (id: string) => lastValueFrom(service.delete(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vehicles', companyId()] }),
  }));
}
