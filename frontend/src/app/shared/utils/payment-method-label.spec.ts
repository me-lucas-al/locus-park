import { paymentMethodLabel } from './payment-method-label';

describe('paymentMethodLabel', () => {
  it('deve traduzir cada forma de pagamento para portugues', () => {
    expect(paymentMethodLabel('DINHEIRO')).toBe('Dinheiro');
    expect(paymentMethodLabel('PIX')).toBe('PIX');
    expect(paymentMethodLabel('CARD_CREDIT')).toBe('Cartão de Crédito');
    expect(paymentMethodLabel('CARD_DEBIT')).toBe('Cartão de Débito');
  });
});
