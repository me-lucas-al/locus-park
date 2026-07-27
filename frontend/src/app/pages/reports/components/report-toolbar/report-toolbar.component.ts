import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DateRangePicker } from '../../../../shared/components/date-range-picker/date-range-picker';
import { DateRange } from '../../../../core/types/date-range.types';
import { ReportExportFormat } from '../../../../core/domains/report/report.types';

@Component({
  selector: 'app-report-toolbar',
  imports: [CommonModule, DateRangePicker],
  templateUrl: './report-toolbar.component.html',
  styleUrl: './report-toolbar.component.css',
})
export class ReportToolbar {
  readonly exportingFormat = input<ReportExportFormat | null>(null);

  readonly rangeChange = output<DateRange>();
  readonly exportRequested = output<ReportExportFormat>();

  protected readonly formats: readonly ReportExportFormat[] = ['pdf', 'xlsx', 'csv'];

  protected onRangeChange(range: DateRange): void {
    this.rangeChange.emit(range);
  }

  protected requestExport(format: ReportExportFormat): void {
    this.exportRequested.emit(format);
  }

  protected isPending(format: ReportExportFormat): boolean {
    return this.exportingFormat() === format;
  }

  protected isDisabled(): boolean {
    return this.exportingFormat() !== null;
  }
}
