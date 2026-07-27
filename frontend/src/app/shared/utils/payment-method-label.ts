import { PaymentMethod } from '../../core/domains/ticket/ticket.types';

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  DINHEIRO: 'Dinheiro',
  PIX: 'PIX',
  CARD_CREDIT: 'Cartão de Crédito',
  CARD_DEBIT: 'Cartão de Débito',
};

export function paymentMethodLabel(method: PaymentMethod): string {
  return PAYMENT_METHOD_LABELS[method];
}
