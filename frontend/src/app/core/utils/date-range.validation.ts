import { daysBetween, isIsoDate, toIsoDate } from './date-iso';
import { DateRange, DateRangeValidation } from '../types/date-range.types';

export const MAX_RANGE_DAYS = 366;

export function validateRange(range: DateRange, now: Date): DateRangeValidation {
  if (!isIsoDate(range.from) || !isIsoDate(range.to)) {
    return { valid: false, error: 'INVALID_DATE' };
  }
  if (range.from > range.to) {
    return { valid: false, error: 'FROM_AFTER_TO' };
  }
  if (range.to > toIsoDate(now)) {
    return { valid: false, error: 'FUTURE_DATE' };
  }
  if (daysBetween(range.from, range.to) >= MAX_RANGE_DAYS) {
    return { valid: false, error: 'RANGE_TOO_LONG' };
  }
  return { valid: true };
}
