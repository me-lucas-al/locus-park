import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideQueryClient } from '@tanstack/angular-query-experimental';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { useVehicleBrandsQuery, useVehicleModelsByBrandQuery } from './vehicle-catalog.hooks';
import { OUTRO_OPTION } from './vehicle-catalog.types';
import { VehicleType } from '../../types/domain-enums.types';

describe('vehicle-catalog.hooks', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

    TestBed.configureTestingModule({
      providers: [provideQueryClient(queryClient)],
    });
  });

  describe('useVehicleBrandsQuery', () => {
    it('deve retornar marcas de carros quando vehicleType é CAR', async () => {
      const vehicleType = signal<VehicleType>('CAR');
      const query = TestBed.runInInjectionContext(() => useVehicleBrandsQuery(vehicleType));

      const brands = await query.refetch();
      expect(brands.data).toBeDefined();
      expect(brands.data?.length).toBeGreaterThan(1);
      expect(brands.data?.some(b => b.label === 'Chevrolet' || b.label === 'Fiat' || b.label === 'Ford')).toBe(true);
      expect(brands.data?.at(-1)).toEqual(OUTRO_OPTION);
    });

    it('deve retornar marcas de motos quando vehicleType é MOTORCYCLE', async () => {
      const vehicleType = signal<VehicleType>('MOTORCYCLE');
      const query = TestBed.runInInjectionContext(() => useVehicleBrandsQuery(vehicleType));

      const brands = await query.refetch();
      expect(brands.data).toBeDefined();
      expect(brands.data?.length).toBeGreaterThan(1);
      expect(brands.data?.some(b => b.label.toUpperCase().includes('HONDA') || b.label.toUpperCase().includes('YAMAHA'))).toBe(true);
      expect(brands.data?.at(-1)).toEqual(OUTRO_OPTION);
    });

    it('deve retornar marcas de caminhões quando vehicleType é TRUCK', async () => {
      const vehicleType = signal<VehicleType>('TRUCK');
      const query = TestBed.runInInjectionContext(() => useVehicleBrandsQuery(vehicleType));

      const brands = await query.refetch();
      expect(brands.data).toBeDefined();
      expect(brands.data?.length).toBeGreaterThan(1);
      expect(brands.data?.some(b => b.label.toUpperCase().includes('SCANIA') || b.label.toUpperCase().includes('VOLVO'))).toBe(true);
      expect(brands.data?.at(-1)).toEqual(OUTRO_OPTION);
    });
  });

  describe('useVehicleModelsByBrandQuery', () => {
    it('deve retornar modelos de moto para marca selecionada (ex: Honda)', async () => {
      const vehicleType = signal<VehicleType>('MOTORCYCLE');
      const selectedBrand = signal('Honda');

      const query = TestBed.runInInjectionContext(() =>
        useVehicleModelsByBrandQuery(selectedBrand, vehicleType)
      );

      const models = await query.refetch();
      expect(models.data).toBeDefined();
      expect(models.data?.length).toBeGreaterThan(1);
      expect(models.data?.at(-1)).toEqual(OUTRO_OPTION);
    });

    it('deve retornar modelos de caminhão para marca selecionada (ex: Scania)', async () => {
      const vehicleType = signal<VehicleType>('TRUCK');
      const selectedBrand = signal('Scania');

      const query = TestBed.runInInjectionContext(() =>
        useVehicleModelsByBrandQuery(selectedBrand, vehicleType)
      );

      const models = await query.refetch();
      expect(models.data).toBeDefined();
      expect(models.data?.length).toBeGreaterThan(1);
      expect(models.data?.at(-1)).toEqual(OUTRO_OPTION);
    });
  });
});
