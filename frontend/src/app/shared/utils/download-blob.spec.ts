import { downloadBlob } from './download-blob';

describe('downloadBlob', () => {
  beforeEach(() => {
    (URL as unknown as { createObjectURL: () => string }).createObjectURL = vi.fn(() => 'blob:x');
    (URL as unknown as { revokeObjectURL: () => void }).revokeObjectURL = vi.fn();
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
  });

  it('deve criar um anchor oculto, disparar o click e revogar a URL', () => {
    const blob = new Blob(['conteudo'], { type: 'text/csv' });
    downloadBlob(blob, 'relatorio.csv');

    expect(URL.createObjectURL).toHaveBeenCalledWith(blob);
    expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:x');
  });
});
