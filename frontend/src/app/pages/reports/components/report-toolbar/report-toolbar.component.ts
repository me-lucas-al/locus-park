import { Component, ElementRef, HostListener, inject, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DateRangePicker } from '../../../../shared/components/date-range-picker/date-range-picker';
import { DateRange } from '../../../../core/types/date-range.types';
import { ReportExportFormat } from '../../../../core/domains/report/report.types';

interface ExportFormatOption {
  readonly format: ReportExportFormat;
  readonly label: string;
}

@Component({
  selector: 'app-report-toolbar',
  imports: [CommonModule, DateRangePicker],
  templateUrl: './report-toolbar.component.html',
  styleUrl: './report-toolbar.component.css',
})
export class ReportToolbar {
  private readonly elementRef = inject(ElementRef);

  readonly exportingFormat = input<ReportExportFormat | null>(null);

  readonly rangeChange = output<DateRange>();
  readonly exportRequested = output<ReportExportFormat>();

  protected readonly isMenuOpen = signal(false);

  protected readonly formatOptions: readonly ExportFormatOption[] = [
    { format: 'pdf', label: 'PDF' },
    { format: 'xlsx', label: 'XLSX' },
    { format: 'csv', label: 'CSV' },
  ];

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isMenuOpen.set(false);
    }
  }

  protected onRangeChange(range: DateRange): void {
    this.rangeChange.emit(range);
  }

  protected toggleMenu(): void {
    if (this.isDisabled()) return;
    this.isMenuOpen.update((open) => !open);
  }

  protected requestExport(format: ReportExportFormat): void {
    this.isMenuOpen.set(false);
    this.exportRequested.emit(format);
  }

  protected isDisabled(): boolean {
    return this.exportingFormat() !== null;
  }
}
