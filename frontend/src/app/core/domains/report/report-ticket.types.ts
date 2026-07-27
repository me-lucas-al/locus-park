import { PaymentMethod } from '../ticket/ticket.types';
import { TicketStatus, VehicleType } from '../../types/domain-enums.types';

export interface TicketRow {
  readonly ticketId: string;
  readonly status: TicketStatus;
  readonly plate: string;
  readonly model: string;
  readonly color: string;
  readonly vehicleType: VehicleType;
  readonly clientName: string | null;
  readonly clientCpf: string | null;
  readonly enteredAt: string;
  readonly exitedAt: string | null;
  readonly stayMinutes: number | null;
  readonly partnershipName: string | null;
  readonly paymentMethod: PaymentMethod | null;
  readonly grossAmount: number | null;
  readonly discountAmount: number | null;
  readonly totalAmount: number | null;
}
