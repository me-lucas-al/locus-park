import { inject, Signal } from '@angular/core';
import { injectQuery, injectMutation, QueryClient } from '@tanstack/angular-query-experimental';
import { lastValueFrom } from 'rxjs';
import { UserService } from './user.service';
import { UpdateUserParams, UpdateUserRoleParams, CreateCollaboratorParams } from './user.types';

export function useUserProfileQuery() {
  const service = inject(UserService);
  return injectQuery(() => ({
    queryKey: ['profile'] as const,
    queryFn: () => lastValueFrom(service.getProfile()),
    staleTime: Infinity,
  }));
}

export function useUsersByCompanyQuery(companyId: Signal<string>) {
  const service = inject(UserService);
  return injectQuery(() => ({
    queryKey: ['users', 'company', companyId()] as const,
    queryFn: () => lastValueFrom(service.getByCompany(companyId())),
    enabled: !!companyId() && companyId() !== 'null' && companyId() !== 'undefined',
    staleTime: Infinity,
  }));
}

export function useCreateCollaboratorMutation() {
  const service = inject(UserService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: CreateCollaboratorParams) =>
      lastValueFrom(service.createCollaborator(params.companyId, params.request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  }));
}

export function useUpdateUserMutation() {
  const service = inject(UserService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: UpdateUserParams) =>
      lastValueFrom(service.update(params.id, params.request)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['profile'] });
    },
  }));
}

export function useDeleteUserMutation() {
  const service = inject(UserService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (id: string) => lastValueFrom(service.delete(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  }));
}

export function useUpdateUserRoleMutation() {
  const service = inject(UserService);
  const queryClient = inject(QueryClient);
  return injectMutation(() => ({
    mutationFn: (params: UpdateUserRoleParams) =>
      lastValueFrom(service.updateRole(params.id, params.request)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
  }));
}
