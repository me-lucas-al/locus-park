import { Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { SelectOption, OUTRO_OPTION } from './vehicle-catalog.types';
import catalogo from './vehicles-catalog.json';

export function useVehicleBrandsQuery() {
  return injectQuery(() => ({
    queryKey: ['vehicle-catalog', 'brands'] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const options = catalogo.map(item => ({ code: item.marca, label: item.marca }));
      options.sort((a, b) => a.label.localeCompare(b.label));
      return [...options, OUTRO_OPTION];
    },
    staleTime: Infinity,
  }));
}

export function useVehicleModelsByBrandQuery(selectedBrandCode: Signal<string>) {
  return injectQuery(() => ({
    queryKey: ['vehicle-catalog', 'models', selectedBrandCode()] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const brandName = selectedBrandCode();
      const brand = catalogo.find(item => item.marca === brandName);
      if (!brand) return [OUTRO_OPTION];

      const options = brand.modelos.map(model => ({
        code: String(model.codigoReferencia),
        label: model.nome,
      }));
      options.sort((a, b) => a.label.localeCompare(b.label));
      return [...options, OUTRO_OPTION];
    },
    enabled: !!selectedBrandCode() && selectedBrandCode() !== OUTRO_OPTION.code,
    staleTime: Infinity,
  }));
}
