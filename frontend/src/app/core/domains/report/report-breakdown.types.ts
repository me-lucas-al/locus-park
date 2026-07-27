import { PaymentMethod } from '../ticket/ticket.types';
import { VehicleType } from '../../types/domain-enums.types';

export interface PaymentMethodSummary {
  readonly method: PaymentMethod;
  readonly ticketCount: number;
  readonly revenue: number;
  readonly sharePercent: number;
}

export interface VehicleTypeSummary {
  readonly type: VehicleType;
  readonly ticketCount: number;
  readonly revenue: number;
  readonly sharePercent: number;
}

export interface DailySummary {
  readonly date: string;
  readonly entryCount: number;
  readonly exitCount: number;
  readonly revenue: number;
  readonly discount: number;
}

export interface HourlySummary {
  readonly hour: number;
  readonly entryCount: number;
  readonly exitCount: number;
  readonly revenue: number;
}

export interface PartnershipSummary {
  readonly partnershipId: string;
  readonly name: string;
  readonly usageCount: number;
  readonly discountGranted: number;
}

export interface ClientSummary {
  readonly clientId: string;
  readonly name: string;
  readonly cpf: string;
  readonly ticketCount: number;
  readonly totalSpent: number;
  readonly averageStayMinutes: number;
  readonly paymentMethodsUsed: readonly PaymentMethod[];
}
