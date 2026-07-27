import { inject, Signal } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { TicketService } from './ticket.service';
import { ApplyPartnershipParams, CheckOutParams } from './ticket.types';
import { AuthService } from '../auth/auth.service';

export function useTicketsQuery() {
  const service = inject(TicketService);
  const authService = inject(AuthService);
  return injectQuery(() => ({
    queryKey: ['tickets', 'active'] as const,
    queryFn: () => lastValueFrom(service.getAll()),
    enabled: !!authService.token() && !!authService.companyId() && authService.companyId() !== 'null' && authService.companyId() !== 'undefined',
  }));
}

export function useTicketByIdQuery(id: Signal<string>) {
  const service = inject(TicketService);
  return injectQuery(() => ({
    queryKey: ['tickets', id()] as const,
    queryFn: () => lastValueFrom(service.getById(id())),
    enabled: !!id(),
  }));
}

export function useCheckInMutation() {
  const service = inject(TicketService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (vehicleId: string) => lastValueFrom(service.checkIn(vehicleId)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tickets'] }),
  }));
}

export function useCheckOutMutation() {
  const service = inject(TicketService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: CheckOutParams) => lastValueFrom(service.checkOut(params.id, params.paymentMethod)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      queryClient.invalidateQueries({ queryKey: ['reports'] });
    },
  }));
}

export function useDeleteTicketMutation() {
  const service = inject(TicketService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (id: string) => lastValueFrom(service.delete(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tickets'] }),
  }));
}

export function useApplyPartnershipMutation() {
  const service = inject(TicketService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: ApplyPartnershipParams) =>
      lastValueFrom(service.applyPartnership(params.id, params.partnershipId)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tickets'] }),
  }));
}
