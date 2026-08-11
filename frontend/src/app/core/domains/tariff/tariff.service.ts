import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  TariffConfigurationRequest,
  TariffConfigurationResponse,
  PricingConfigurationRequest,
  PricingConfigurationResponse,
  VehicleTypePricingBatchRequest,
  VehicleTypeMultiplierResponse,
} from './tariff.types';

@Injectable({ providedIn: 'root' })
export class TariffService {
  private readonly http = inject(HttpClient);
  private readonly tariffUrl = `${environment.apiUrl}configurations/tariff`;
  private readonly pricingUrl = `${environment.apiUrl}configurations/pricing`;
  private readonly vehiclePricingUrl = `${environment.apiUrl}configurations/vehicle-pricing`;

  getTariff(): Observable<TariffConfigurationResponse> {
    return this.http.get<TariffConfigurationResponse>(this.tariffUrl);
  }

  updateTariff(request: TariffConfigurationRequest): Observable<TariffConfigurationResponse> {
    return this.http.put<TariffConfigurationResponse>(this.tariffUrl, request);
  }

  deleteTariff(): Observable<void> {
    return this.http.delete<void>(this.tariffUrl);
  }

  getPricing(): Observable<PricingConfigurationResponse> {
    return this.http.get<PricingConfigurationResponse>(this.pricingUrl);
  }

  updatePricing(request: PricingConfigurationRequest): Observable<PricingConfigurationResponse> {
    return this.http.put<PricingConfigurationResponse>(this.pricingUrl, request);
  }

  deletePricing(): Observable<void> {
    return this.http.delete<void>(this.pricingUrl);
  }

  getVehiclePricing(): Observable<VehicleTypeMultiplierResponse[]> {
    return this.http.get<VehicleTypeMultiplierResponse[]>(this.vehiclePricingUrl);
  }

  updateVehiclePricing(request: VehicleTypePricingBatchRequest): Observable<VehicleTypeMultiplierResponse[]> {
    return this.http.put<VehicleTypeMultiplierResponse[]>(this.vehiclePricingUrl, request);
  }
}
