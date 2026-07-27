import { Component, computed, input } from '@angular/core';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

export interface BarChartPoint {
  readonly label: string;
  readonly value: number;
}

@Component({
  selector: 'app-report-bar-chart',
  imports: [EmptyState],
  templateUrl: './report-bar-chart.component.html',
  styleUrl: './report-bar-chart.component.css',
})
export class ReportBarChart {
  readonly title = input.required<string>();
  readonly points = input.required<readonly BarChartPoint[]>();

  protected readonly maxValue = computed(() => Math.max(1, ...this.points().map((p) => p.value)));
}
