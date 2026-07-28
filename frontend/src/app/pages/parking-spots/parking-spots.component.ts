import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { useTicketsQuery } from '../../core/domains/ticket/ticket.hooks';
import { TicketResponse } from '../../core/domains/ticket/ticket.types';
import { ModalExit } from '../../shared/components/modal-exit/modal-exit.component';
import { SpotAssignmentService } from '../../shared/services/spot-assignment.service';
import { ParkingMap } from './components/parking-map/parking-map.component';
import { ParkingSpotForm } from './components/parking-spot-form/parking-spot-form.component';

interface GridSpot {
  number: number;
  ticket: TicketResponse | null;
  status: 'Livre' | 'Ocupada';
}

@Component({
  selector: 'app-parking-spots',
  standalone: true,
  imports: [CommonModule, ModalExit, ParkingMap, ParkingSpotForm],
  templateUrl: './parking-spots.component.html',
  styleUrl: './parking-spots.component.css',
})
export class ParkingSpots {
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  protected readonly ticketsQuery = useTicketsQuery();

  readonly totalSpots = 120;
  readonly modalOpen = signal(false);
  readonly selectedTicket = signal<TicketResponse | null>(null);
  readonly selectedSpotForRegister = signal<number | null>(null);

  protected readonly gridSpots = computed<GridSpot[]>(() => {
    const rawTickets = this.ticketsQuery.data() || [];
    const activeTickets = rawTickets.filter((t) => !t.exitedAt && t.status === 'ACTIVE');
    this.spotAssignmentService.cleanInactiveTickets(activeTickets);

    const ticketMap = new Map<number, TicketResponse>();
    activeTickets.forEach((t) => {
      const spot = this.spotAssignmentService.getSpot(t);
      if (spot) {
        ticketMap.set(spot, t);
      }
    });

    const spots: GridSpot[] = [];
    for (let i = 1; i <= this.totalSpots; i++) {
      const ticket = ticketMap.get(i) || null;
      spots.push({
        number: i,
        ticket,
        status: ticket ? 'Ocupada' : 'Livre',
      });
    }
    return spots;
  });

  protected handleSpotClick(spot: GridSpot): void {
    if (spot.status === 'Ocupada' && spot.ticket) {
      this.selectedTicket.set(spot.ticket);
      this.modalOpen.set(true);
      this.selectedSpotForRegister.set(null);
      return;
    }
    this.selectedSpotForRegister.set(spot.number);
  }

  protected closeCheckoutModal(): void {
    this.modalOpen.set(false);
    this.selectedTicket.set(null);
  }

  protected handleCheckoutConfirmed(): void {
    this.ticketsQuery.refetch();
  }

  protected onFormConfirmed(): void {
    this.ticketsQuery.refetch();
    this.selectedSpotForRegister.set(null);
  }

  protected onFormCancel(): void {
    this.selectedSpotForRegister.set(null);
  }
}
