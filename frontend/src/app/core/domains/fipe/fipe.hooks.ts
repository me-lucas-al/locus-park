import { Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { SelectOption, OUTRO_OPTION } from './fipe.types';
import catalogo from './catalogo-fipe-limpo.json';

export function useFipeBrandsQuery() {
  return injectQuery(() => ({
    queryKey: ['fipe', 'brands'] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const options = catalogo.map(item => ({ code: item.marca, label: item.marca }));
      options.sort((a, b) => a.label.localeCompare(b.label));
      return [...options, OUTRO_OPTION];
    },
    staleTime: Infinity,
  }));
}

export function useFipeModelsByBrandQuery(selectedBrandCode: Signal<string>) {
  return injectQuery(() => ({
    queryKey: ['fipe', 'models', selectedBrandCode()] as const,
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
