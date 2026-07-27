import { Component, output, signal } from '@angular/core';
import { DateRange, DateRangeErrorCode, DateRangePreset } from '../../../core/types/date-range.types';
import { DEFAULT_RANGE_PRESET, PRESET_OPTIONS, buildRange } from '../../../core/utils/date-range.factory';
import { validateRange } from '../../../core/utils/date-range.validation';
import { toIsoDate } from '../../../core/utils/date-iso';

const ERROR_MESSAGES: Record<DateRangeErrorCode, string> = {
  INVALID_DATE: 'Data inválida.',
  FROM_AFTER_TO: 'A data inicial não pode ser posterior à data final.',
  FUTURE_DATE: 'A data final não pode estar no futuro.',
  RANGE_TOO_LONG: 'O período não pode exceder 366 dias.',
};

@Component({
  selector: 'app-date-range-picker',
  imports: [],
  templateUrl: './date-range-picker.html',
  styleUrl: './date-range-picker.css',
})
export class DateRangePicker {
  protected readonly presetOptions = PRESET_OPTIONS;
  protected readonly today = toIsoDate(new Date());

  protected readonly selection = signal<DateRangePreset | 'CUSTOM'>(DEFAULT_RANGE_PRESET);
  protected readonly range = signal<DateRange>(buildRange(DEFAULT_RANGE_PRESET, new Date()));
  protected readonly errorMessage = signal<string | null>(null);

  readonly rangeChange = output<DateRange>();

  protected selectPreset(preset: DateRangePreset): void {
    this.selection.set(preset);
    const next = buildRange(preset, new Date());
    this.range.set(next);
    this.errorMessage.set(null);
    this.rangeChange.emit(next);
  }

  protected updateFrom(event: Event): void {
    this.applyCustom({ ...this.range(), from: (event.target as HTMLInputElement).value });
  }

  protected updateTo(event: Event): void {
    this.applyCustom({ ...this.range(), to: (event.target as HTMLInputElement).value });
  }

  private applyCustom(next: DateRange): void {
    this.selection.set('CUSTOM');
    this.range.set(next);
    const validation = validateRange(next, new Date());
    if (!validation.valid) {
      this.errorMessage.set(ERROR_MESSAGES[validation.error!]);
      return;
    }
    this.errorMessage.set(null);
    this.rangeChange.emit(next);
  }
}
