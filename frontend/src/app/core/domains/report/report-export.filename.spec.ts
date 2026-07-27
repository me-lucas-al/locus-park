import { reportExportFallbackFilename } from './report-export.filename';

describe('reportExportFallbackFilename', () => {
  it('deve montar o nome seguindo a convencao do backend', () => {
    const name = reportExportFallbackFilename('pdf', { from: '2026-07-01', to: '2026-07-31' });
    expect(name).toBe('relatorio-locus-park-2026-07-01-a-2026-07-31.pdf');
  });

  it('deve usar a extensao correspondente a cada formato', () => {
    const range = { from: '2026-07-01', to: '2026-07-01' };
    expect(reportExportFallbackFilename('csv', range).endsWith('.csv')).toBe(true);
    expect(reportExportFallbackFilename('xlsx', range).endsWith('.xlsx')).toBe(true);
  });
});
