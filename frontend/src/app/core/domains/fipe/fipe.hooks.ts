import { inject, Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { FipeService } from './fipe.service';
import { SelectOption } from './fipe.types';

export function useFipeBrandsQuery() {
  const fipeService = inject(FipeService);

  return injectQuery(() => ({
    queryKey: ['fipe', 'brands'] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const brands = await fipeService.fetchBrands();
      return brands.map(brand => ({ code: brand.codigo, label: brand.nome }));
    },
    staleTime: 1000 * 60 * 60,
  }));
}

export function useFipeModelsByBrandQuery(selectedBrandCode: Signal<string>) {
  const fipeService = inject(FipeService);

  return injectQuery(() => ({
    queryKey: ['fipe', 'models', selectedBrandCode()] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const response = await fipeService.fetchModelsByBrand(selectedBrandCode());
      return response.modelos.map(model => ({
        code: String(model.codigo),
        label: model.nome,
      }));
    },
    enabled: !!selectedBrandCode(),
    staleTime: 1000 * 60 * 60,
  }));
}
