import { Component, computed, inject, signal } from '@angular/core';
import { useReportQuery } from '../../core/domains/report/report.hooks';
import { useReportExportMutation } from '../../core/domains/report/report-export.hooks';
import { DEFAULT_RANGE_PRESET, buildRange } from '../../core/utils/date-range.factory';
import { DateRange } from '../../core/types/date-range.types';
import { ReportExportFormat } from '../../core/domains/report/report.types';
import { ToastService } from '../../shared/services/toast.service';
import { readBlobErrorMessage } from '../../shared/utils/blob-error';
import { ReportToolbar } from './components/report-toolbar/report-toolbar.component';
import { ReportKpisRevenue } from './components/report-kpis-revenue/report-kpis-revenue.component';
import { ReportKpisStay } from './components/report-kpis-stay/report-kpis-stay.component';
import { ReportKpisOccupancy } from './components/report-kpis-occupancy/report-kpis-occupancy.component';
import { ReportBarChart } from './components/report-bar-chart/report-bar-chart.component';
import { ReportTable } from './components/report-table/report-table.component';
import { InlineNotice } from '../../shared/components/inline-notice/inline-notice';
import { PAYMENT_METHOD_COLUMNS, toPaymentMethodRows } from './tables/payment-method.table';
import { VEHICLE_TYPE_COLUMNS, toVehicleTypeRows } from './tables/vehicle-type.table';
import { DAILY_COLUMNS, toDailyRows } from './tables/daily.table';
import { HOURLY_COLUMNS, toHourlyRows } from './tables/hourly.table';
import { PARTNERSHIP_COLUMNS, toPartnershipRows } from './tables/partnership.table';
import { CLIENT_COLUMNS, toClientRows } from './tables/client.table';
import { TICKET_COLUMNS, toTicketRows } from './tables/ticket.table';

@Component({
  selector: 'app-reports',
  imports: [
    ReportToolbar, ReportKpisRevenue, ReportKpisStay, ReportKpisOccupancy,
    ReportBarChart, ReportTable, InlineNotice,
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css',
})
export class Reports {
  private readonly toastService = inject(ToastService);

  protected readonly range = signal<DateRange>(buildRange(DEFAULT_RANGE_PRESET, new Date()));
  protected readonly reportQuery = useReportQuery(this.range);
  protected readonly exportMutation = useReportExportMutation();

  protected readonly exportingFormat = computed<ReportExportFormat | null>(() =>
    this.exportMutation.isPending() ? (this.exportMutation.variables()?.format ?? null) : null,
  );

  protected readonly paymentMethodColumns = PAYMENT_METHOD_COLUMNS;
  protected readonly vehicleTypeColumns = VEHICLE_TYPE_COLUMNS;
  protected readonly dailyColumns = DAILY_COLUMNS;
  protected readonly hourlyColumns = HOURLY_COLUMNS;
  protected readonly partnershipColumns = PARTNERSHIP_COLUMNS;
  protected readonly clientColumns = CLIENT_COLUMNS;
  protected readonly ticketColumns = TICKET_COLUMNS;

  protected readonly paymentMethodRows = computed(() => toPaymentMethodRows(this.reportQuery.data()?.paymentMethodSummaries ?? []));
  protected readonly vehicleTypeRows = computed(() => toVehicleTypeRows(this.reportQuery.data()?.vehicleTypeSummaries ?? []));
  protected readonly dailyRows = computed(() => toDailyRows(this.reportQuery.data()?.dailySummaries ?? []));
  protected readonly hourlyRows = computed(() => toHourlyRows(this.reportQuery.data()?.hourlySummaries ?? []));
  protected readonly partnershipRows = computed(() => toPartnershipRows(this.reportQuery.data()?.partnershipSummaries ?? []));
  protected readonly clientRows = computed(() => toClientRows(this.reportQuery.data()?.clientSummaries ?? []));
  protected readonly ticketRows = computed(() => toTicketRows(this.reportQuery.data()?.tickets ?? []));

  protected readonly revenueChartPoints = computed(() =>
    (this.reportQuery.data()?.dailySummaries ?? []).map((d) => ({ label: d.date.slice(8, 10), value: d.revenue })),
  );
  protected readonly entriesChartPoints = computed(() =>
    (this.reportQuery.data()?.dailySummaries ?? []).map((d) => ({ label: d.date.slice(8, 10), value: d.entryCount })),
  );

  protected onRangeChange(range: DateRange): void {
    this.range.set(range);
  }

  protected onExportRequested(format: ReportExportFormat): void {
    this.exportMutation.mutate(
      { format, range: this.range() },
      {
        onError: async (error: unknown) => {
          const message = await readBlobErrorMessage(error, 'Não foi possível exportar o relatório.');
          this.toastService.error(message);
        },
      },
    );
  }
}
