import { inject } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { TariffService } from './tariff.service';
import {
  TariffConfigurationRequest,
  PricingConfigurationRequest,
  VehicleTypePricingBatchRequest,
} from './tariff.types';

export function useTariffQuery() {
  const service = inject(TariffService);
  return injectQuery(() => ({
    queryKey: ['tariff'] as const,
    queryFn: () => lastValueFrom(service.getTariff()),
    staleTime: Infinity,
  }));
}

export function useUpdateTariffMutation() {
  const service = inject(TariffService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: TariffConfigurationRequest) => lastValueFrom(service.updateTariff(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tariff'] }),
  }));
}

export function useDeleteTariffMutation() {
  const service = inject(TariffService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: () => lastValueFrom(service.deleteTariff()),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tariff'] }),
  }));
}

export function usePricingQuery() {
  const service = inject(TariffService);
  return injectQuery(() => ({
    queryKey: ['pricing'] as const,
    queryFn: () => lastValueFrom(service.getPricing()),
    staleTime: Infinity,
  }));
}

export function useUpdatePricingMutation() {
  const service = inject(TariffService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: PricingConfigurationRequest) => lastValueFrom(service.updatePricing(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pricing'] }),
  }));
}

export function useDeletePricingMutation() {
  const service = inject(TariffService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: () => lastValueFrom(service.deletePricing()),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['pricing'] }),
  }));
}

export function useVehiclePricingQuery() {
  const service = inject(TariffService);
  return injectQuery(() => ({
    queryKey: ['vehiclePricing'] as const,
    queryFn: () => lastValueFrom(service.getVehiclePricing()),
    staleTime: Infinity,
  }));
}

export function useUpdateVehiclePricingMutation() {
  const service = inject(TariffService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (request: VehicleTypePricingBatchRequest) => lastValueFrom(service.updateVehiclePricing(request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['vehiclePricing'] }),
  }));
}
