import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { useTicketsQuery } from '../../core/domains/ticket/ticket.hooks';
import { TicketResponse } from '../../core/domains/ticket/ticket.types';
import { ModalExit } from '../../shared/components/modal-exit/modal-exit.component';
import { SpotAssignmentService } from '../../shared/services/spot-assignment.service';

@Component({
  selector: 'app-exit',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalExit],
  templateUrl: './exit.component.html',
  styleUrl: './exit.component.css',
})
export class Exit {
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  // Queries
  protected readonly ticketsQuery = useTicketsQuery();

  // Estados locais
  readonly searchTerm = signal('');
  readonly isSearchFocused = signal(false);
  readonly modalOpen = signal(false);
  readonly selectedTicket = signal<TicketResponse | null>(null);

  protected getSpotNumber(ticket: TicketResponse): number {
    return this.spotAssignmentService.getSpot(ticket);
  }

  // Filtro de tickets ativos por placa ou modelo
  protected readonly filteredTickets = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const rawTickets = this.ticketsQuery.data() || [];
    const activeTickets = rawTickets.filter((t) => !t.exitedAt && t.status === 'ACTIVE');

    if (!term) return [];

    return activeTickets.filter(
      (t) =>
        t.vehicle.plate.toLowerCase().includes(term) ||
        t.vehicle.model.toLowerCase().includes(term)
    );
  });

  protected selectTicket(ticket: TicketResponse): void {
    this.selectedTicket.set(ticket);
    this.modalOpen.set(true);
  }

  protected closeCheckoutModal(): void {
    this.modalOpen.set(false);
    this.selectedTicket.set(null);
  }

  protected handleCheckoutConfirmed(): void {
    this.clearSearch();
    // Forçar recarregamento da query
    this.ticketsQuery.refetch();
  }

  protected clearSearch(): void {
    this.searchTerm.set('');
  }

  protected onSearchFocus(): void {
    this.isSearchFocused.set(true);
  }

  protected onSearchBlur(): void {
    // Pequeno delay para permitir o clique nos resultados antes de esconder
    setTimeout(() => {
      this.isSearchFocused.set(false);
    }, 200);
  }
}
