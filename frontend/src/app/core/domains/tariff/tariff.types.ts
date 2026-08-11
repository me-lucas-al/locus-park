export type VehicleType = 'CAR' | 'MOTORCYCLE' | 'VAN' | 'TRUCK';

export interface TariffConfigurationRequest {
  toleranceMinutes: number;
  firstHourValue: number;
  additionalFractionValue: number;
  overnightFee: number;
  lostTicketFee: number;
}

export interface TariffConfigurationResponse {
  id: string;
  companyId: string;
  toleranceMinutes: number;
  firstHourValue: number;
  additionalFractionValue: number;
  overnightFee: number;
  lostTicketFee: number;
}

export interface PricingConfigurationRequest {
  dailyTriggerHours: number;
  dailyValue: number;
  monthlyBaseValue: number;
}

export interface PricingConfigurationResponse {
  id: string;
  companyId: string;
  dailyTriggerHours: number;
  dailyValue: number;
  monthlyBaseValue: number;
}

export interface VehicleTypeMultiplierItemRequest {
  vehicleType: VehicleType;
  multiplier: number;
}

export interface VehicleTypePricingBatchRequest {
  multipliers: VehicleTypeMultiplierItemRequest[];
}

export interface VehicleTypeMultiplierResponse {
  id: string | null;
  vehicleType: VehicleType;
  multiplier: number;
  label: string;
}
