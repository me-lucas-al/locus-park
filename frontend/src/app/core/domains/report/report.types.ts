import { ReportCompany, ReportPeriod, ReportSummary } from './report-summary.types';
import {
  ClientSummary,
  DailySummary,
  HourlySummary,
  PartnershipSummary,
  PaymentMethodSummary,
  VehicleTypeSummary,
} from './report-breakdown.types';
import { TicketRow } from './report-ticket.types';

export * from './report-summary.types';
export * from './report-breakdown.types';
export * from './report-ticket.types';

export type ReportExportFormat = 'pdf' | 'csv' | 'xlsx';

export interface ReportResponse {
  readonly period: ReportPeriod;
  readonly company: ReportCompany;
  readonly summary: ReportSummary;
  readonly paymentMethodSummaries: readonly PaymentMethodSummary[];
  readonly vehicleTypeSummaries: readonly VehicleTypeSummary[];
  readonly dailySummaries: readonly DailySummary[];
  readonly hourlySummaries: readonly HourlySummary[];
  readonly partnershipSummaries: readonly PartnershipSummary[];
  readonly clientSummaries: readonly ClientSummary[];
  readonly tickets: readonly TicketRow[];
  readonly ticketCount: number;
  readonly ticketsTruncated: boolean;
  readonly totalRevenue: number;
  readonly totalServices: number;
  readonly averageStayMinutes: number;
}
