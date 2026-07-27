export function formatPercent(value: number, fractionDigits = 1): string {
  return `${value.toFixed(fractionDigits).replace('.', ',')}%`;
}
