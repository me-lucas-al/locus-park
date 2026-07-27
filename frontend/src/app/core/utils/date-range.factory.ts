import { addDays, toIsoDate } from './date-iso';
import { DateRange, DateRangePreset, DateRangePresetOption } from '../types/date-range.types';

export const DEFAULT_RANGE_PRESET: DateRangePreset = 'LAST_7_DAYS';

export const PRESET_OPTIONS: readonly DateRangePresetOption[] = [
  { value: 'TODAY', label: 'Hoje' },
  { value: 'LAST_7_DAYS', label: 'Últimos 7 dias' },
  { value: 'THIS_MONTH', label: 'Este mês (até hoje)' },
];

export function buildRange(preset: DateRangePreset, now: Date): DateRange {
  const today = toIsoDate(now);
  if (preset === 'TODAY') {
    return { from: today, to: today };
  }
  if (preset === 'LAST_7_DAYS') {
    return { from: toIsoDate(addDays(now, -6)), to: today };
  }
  const firstOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  return { from: toIsoDate(firstOfMonth), to: today };
}
