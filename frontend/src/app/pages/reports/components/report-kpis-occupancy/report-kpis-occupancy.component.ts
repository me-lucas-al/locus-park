import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SummaryCard } from '../../../../shared/components/summary-card/summary-card';
import { OccupancySummary } from '../../../../core/domains/report/report.types';
import { formatPercent } from '../../../../shared/utils/format-percent';

@Component({
  selector: 'app-report-kpis-occupancy',
  imports: [CommonModule, SummaryCard],
  templateUrl: './report-kpis-occupancy.component.html',
  styleUrl: '../report-kpis-shared.css',
})
export class ReportKpisOccupancy {
  readonly occupancy = input.required<OccupancySummary>();

  protected readonly peakHint = computed(() => {
    const peakAt = this.occupancy().peakAt;
    if (!peakAt) return '';
    const date = new Date(peakAt);
    const hh = String(date.getHours()).padStart(2, '0');
    const mm = String(date.getMinutes()).padStart(2, '0');
    return `às ${hh}:${mm}`;
  });

  protected readonly peakRateLabel = computed(() => formatPercent(this.occupancy().peakOccupancyRate * 100));
  protected readonly averageRateLabel = computed(() => formatPercent(this.occupancy().averageOccupancyRate * 100));
  protected readonly turnoverLabel = computed(() => this.occupancy().turnoverPerSpot.toFixed(2));
}
