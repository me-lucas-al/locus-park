import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SummaryCard } from '../../../../shared/components/summary-card/summary-card';
import { RevenueSummary } from '../../../../core/domains/report/report.types';

@Component({
  selector: 'app-report-kpis-revenue',
  imports: [CommonModule, SummaryCard],
  templateUrl: './report-kpis-revenue.component.html',
  styleUrl: '../report-kpis-shared.css',
})
export class ReportKpisRevenue {
  readonly revenue = input.required<RevenueSummary>();
}
