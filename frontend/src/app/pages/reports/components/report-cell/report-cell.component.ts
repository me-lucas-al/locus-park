import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { formatDurationMinutes } from '../../../../shared/utils/format-duration';
import { formatPercent } from '../../../../shared/utils/format-percent';

export type ReportCellFormat =
  | 'text'
  | 'integer'
  | 'decimal'
  | 'currency'
  | 'percent'
  | 'duration'
  | 'date'
  | 'datetime';

@Component({
  selector: 'app-report-cell',
  imports: [CommonModule],
  templateUrl: './report-cell.component.html',
})
export class ReportCell {
  readonly format = input.required<ReportCellFormat>();
  readonly value = input<unknown>(null);

  protected readonly isEmpty = computed(() => {
    const value = this.value();
    return value === null || value === undefined || value === '';
  });

  protected readonly numberValue = computed(() => (typeof this.value() === 'number' ? (this.value() as number) : 0));
  protected readonly dateValue = computed(() => (this.isEmpty() ? null : new Date(this.value() as string)));
  protected readonly textValue = computed(() => (this.isEmpty() ? '—' : String(this.value())));
  protected readonly durationText = computed(() => (this.isEmpty() ? '—' : formatDurationMinutes(this.numberValue())));
  protected readonly percentText = computed(() => (this.isEmpty() ? '—' : formatPercent(this.numberValue())));
}
