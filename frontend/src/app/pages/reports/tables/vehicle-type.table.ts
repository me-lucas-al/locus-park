import { ReportColumn } from '../components/report-table/report-table.component';
import { VehicleTypeSummary } from '../../../core/domains/report/report.types';
import { vehicleTypeLabel } from '../../../shared/utils/vehicle-type-label';

export interface VehicleTypeRow {
  readonly type: string;
  readonly ticketCount: number;
  readonly revenue: number;
  readonly sharePercent: number;
}

export const VEHICLE_TYPE_COLUMNS: readonly ReportColumn[] = [
  { key: 'type', label: 'Tipo de Veículo', format: 'text' },
  { key: 'ticketCount', label: 'Atendimentos', format: 'integer', align: 'right' },
  { key: 'revenue', label: 'Receita', format: 'currency', align: 'right' },
  { key: 'sharePercent', label: 'Participação', format: 'percent', align: 'right' },
];

export function toVehicleTypeRows(summaries: readonly VehicleTypeSummary[]): VehicleTypeRow[] {
  return summaries.map((s) => ({
    type: vehicleTypeLabel(s.type),
    ticketCount: s.ticketCount,
    revenue: s.revenue,
    sharePercent: s.sharePercent,
  }));
}
