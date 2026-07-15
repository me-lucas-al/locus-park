import { TicketResponse } from '../../core/domains/ticket/ticket.types';

export interface GridSpot {
  number: number;
  ticket: TicketResponse | null;
  status: 'Livre' | 'Ocupada';
}

export interface LayoutItem {
  type: 'spot' | 'corridor' | 'empty';
  spot?: GridSpot;
  corridorIndex?: number;
  direction?: 'left' | 'right';
  spotRowIndex?: number;
  orientation?: 'top-open' | 'bottom-open';
}
