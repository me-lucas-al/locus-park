export interface ReportPeriod {
  readonly from: string;
  readonly to: string;
  readonly days: number;
}

export interface ReportCompany {
  readonly id: string;
  readonly name: string;
  readonly cnpj: string;
  readonly totalSpots: number | null;
}

export interface RevenueSummary {
  readonly grossRevenue: number;
  readonly discountGranted: number;
  readonly netRevenue: number;
  readonly averageTicketValue: number;
  readonly highestTicketValue: number;
  readonly lowestTicketValue: number;
  readonly paidTicketCount: number;
  readonly freeExitCount: number;
}

export interface StaySummary {
  readonly averageMinutes: number;
  readonly minimumMinutes: number;
  readonly maximumMinutes: number;
  readonly totalMinutes: number;
  readonly openStayCount: number;
}

export interface OccupancySummary {
  readonly totalSpots: number;
  readonly entryCount: number;
  readonly exitCount: number;
  readonly activeCount: number;
  readonly peakConcurrentVehicles: number;
  readonly peakAt: string | null;
  readonly peakOccupancyRate: number;
  readonly averageOccupancyRate: number;
  readonly turnoverPerSpot: number;
}

export interface ReportSummary {
  readonly revenue: RevenueSummary;
  readonly stay: StaySummary;
  readonly occupancy: OccupancySummary;
}
