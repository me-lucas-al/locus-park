export type DateRangePreset = 'TODAY' | 'LAST_7_DAYS' | 'THIS_MONTH';

export type DateRangeSelection = DateRangePreset | 'CUSTOM';

export interface DateRange {
  readonly from: string;
  readonly to: string;
}

export type DateRangeErrorCode = 'INVALID_DATE' | 'FROM_AFTER_TO' | 'FUTURE_DATE' | 'RANGE_TOO_LONG';

export interface DateRangeValidation {
  readonly valid: boolean;
  readonly error?: DateRangeErrorCode;
}

export interface DateRangePresetOption {
  readonly value: DateRangePreset;
  readonly label: string;
}
