import { Signal } from '@angular/core';
import { injectQuery } from '@tanstack/angular-query-experimental';
import { SelectOption, OUTRO_OPTION } from './vehicle-catalog.types';
import { VehicleType } from '../../types/domain-enums.types';
import carsCatalog from './vehicles-catalog.json';
import motosCatalog from './motos-catalog.json';
import trucksCatalog from './trucks-catalog.json';

interface CatalogBrand {
  marca: string;
  modelos: Array<{
    nome: string;
    codigoReferencia: number;
  }>;
}

function getCatalogForType(type: VehicleType | string): CatalogBrand[] {
  switch (type) {
    case 'MOTORCYCLE':
      return motosCatalog as CatalogBrand[];
    case 'TRUCK':
      return trucksCatalog as CatalogBrand[];
    case 'CAR':
    case 'VAN':
    default:
      return carsCatalog as CatalogBrand[];
  }
}

export function useVehicleBrandsQuery(vehicleType: Signal<VehicleType | string>) {
  return injectQuery(() => ({
    queryKey: ['vehicle-catalog', 'brands', vehicleType()] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const catalog = getCatalogForType(vehicleType());
      const options = catalog.map(item => ({ code: item.marca, label: item.marca }));
      options.sort((a, b) => a.label.localeCompare(b.label));
      return [...options, OUTRO_OPTION];
    },
    staleTime: Infinity,
  }));
}

export function useVehicleModelsByBrandQuery(
  selectedBrandCode: Signal<string>,
  vehicleType: Signal<VehicleType | string>
) {
  return injectQuery(() => ({
    queryKey: ['vehicle-catalog', 'models', vehicleType(), selectedBrandCode()] as const,
    queryFn: async (): Promise<SelectOption[]> => {
      const brandName = selectedBrandCode();
      const catalog = getCatalogForType(vehicleType());
      const brand = catalog.find(item => item.marca === brandName);
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
