export function formatDurationMinutes(totalMinutes: number | null): string {
  if (totalMinutes === null || totalMinutes < 0) return '—';
  const hours = Math.floor(totalMinutes / 60);
  const minutes = Math.round(totalMinutes % 60);
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
}
