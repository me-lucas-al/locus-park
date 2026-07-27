export function hourLabel(hour: number): string {
  return `${String(hour).padStart(2, '0')}:00`;
}
