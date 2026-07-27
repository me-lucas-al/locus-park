import { filenameFromContentDisposition } from './content-disposition';

describe('filenameFromContentDisposition', () => {
  it('deve extrair nome de arquivo RFC 5987', () => {
    const header = "attachment; filename*=UTF-8''relat%C3%B3rio.pdf";
    expect(filenameFromContentDisposition(header, 'fallback.pdf')).toBe('relatório.pdf');
  });

  it('deve extrair nome de arquivo entre aspas quando nao ha RFC 5987', () => {
    const header = 'attachment; filename="relatorio.csv"';
    expect(filenameFromContentDisposition(header, 'fallback.csv')).toBe('relatorio.csv');
  });

  it('deve preferir RFC 5987 quando ambos os formatos estao presentes', () => {
    const header = "attachment; filename=\"antigo.pdf\"; filename*=UTF-8''novo.pdf";
    expect(filenameFromContentDisposition(header, 'fallback.pdf')).toBe('novo.pdf');
  });

  it('deve extrair nome de arquivo sem aspas', () => {
    const header = 'attachment; filename=relatorio.xlsx';
    expect(filenameFromContentDisposition(header, 'fallback.xlsx')).toBe('relatorio.xlsx');
  });

  it('deve sanitizar tentativa de path traversal para o ultimo segmento', () => {
    const header = 'attachment; filename="../../etc/passwd"';
    expect(filenameFromContentDisposition(header, 'fallback')).toBe('passwd');
  });

  it('deve retornar o fallback quando o header e nulo', () => {
    expect(filenameFromContentDisposition(null, 'fallback.pdf')).toBe('fallback.pdf');
  });

  it('deve retornar o fallback quando o valor sanitizado fica vazio', () => {
    const header = 'attachment; filename=""';
    expect(filenameFromContentDisposition(header, 'fallback.pdf')).toBe('fallback.pdf');
  });
});
