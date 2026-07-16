import { inject, Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { FipeService } from './fipe.service';
import { SelectOption, OUTRO_OPTION } from './fipe.types';

export function useFipeBrandsQuery() {
  const fipeService = inject(FipeService);

  return injectQuery(() => ({
    queryKey: ['fipe', 'brands'] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const brands = await fipeService.fetchBrands();
      const options = brands.map(brand => ({ code: brand.codigo, label: brand.nome }));
      return [...options, OUTRO_OPTION];
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
      const options = response.modelos.map(model => ({
        code: String(model.codigo),
        label: model.nome,
      }));
      return [...options, OUTRO_OPTION];
    },
    enabled: !!selectedBrandCode() && selectedBrandCode() !== OUTRO_OPTION.code,
    staleTime: 1000 * 60 * 60,
  }));
}
