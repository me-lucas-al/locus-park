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
