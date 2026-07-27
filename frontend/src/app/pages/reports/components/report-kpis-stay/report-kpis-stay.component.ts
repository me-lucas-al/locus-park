import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SummaryCard } from '../../../../shared/components/summary-card/summary-card';
import { StaySummary } from '../../../../core/domains/report/report.types';
import { formatDurationMinutes } from '../../../../shared/utils/format-duration';

@Component({
  selector: 'app-report-kpis-stay',
  imports: [CommonModule, SummaryCard],
  templateUrl: './report-kpis-stay.component.html',
  styleUrl: '../report-kpis-shared.css',
})
export class ReportKpisStay {
  readonly stay = input.required<StaySummary>();

  protected readonly averageLabel = computed(() => formatDurationMinutes(this.stay().averageMinutes));
  protected readonly minimumLabel = computed(() => formatDurationMinutes(this.stay().minimumMinutes));
  protected readonly maximumLabel = computed(() => formatDurationMinutes(this.stay().maximumMinutes));
  protected readonly totalLabel = computed(() => formatDurationMinutes(this.stay().totalMinutes));
}
