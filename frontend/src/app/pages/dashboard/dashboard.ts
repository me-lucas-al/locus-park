import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { useTicketsQuery } from '../../core/domains/ticket/ticket.hooks';
import { useReportQuery } from '../../core/domains/report/report.hooks';
import { useUserProfileQuery } from '../../core/domains/user/user.hooks';
import { TicketResponse } from '../../core/domains/ticket/ticket.types';
import { ModalExit } from '../../shared/components/modal-exit/modal-exit.component';
import { ParkingGridComponent } from './components/parking-grid/parking-grid.component';
import { OccupiedListComponent } from './components/occupied-list/occupied-list.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ModalExit, ParkingGridComponent, OccupiedListComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly router = inject(Router);

  protected readonly ticketsQuery = useTicketsQuery();
  protected readonly profileQuery = useUserProfileQuery();

  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || null);
  protected readonly reportQuery = useReportQuery(this.companyId);

  readonly totalSpots = signal<number>(120);
  readonly modalSaidaAberto = signal(false);
  readonly veiculoSelecionado = signal<TicketResponse | null>(null);

  protected readonly occupiedSpotsCount = computed(() => {
    return this.ticketsQuery.data()?.length || 0;
  });

  protected readonly freeSpotsCount = computed(() => {
    return Math.max(0, this.totalSpots() - this.occupiedSpotsCount());
  });

  protected updateTotalSpots(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (!isNaN(value) && value > 0) {
      this.totalSpots.set(value);
    }
  }

  protected formatCurrentDate(): string {
    const date = new Date();
    return date.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  }

  protected navigateToEntry(): void {
    this.router.navigate(['/entry']);
  }

  protected openCheckoutModal(ticket: TicketResponse): void {
    this.veiculoSelecionado.set(ticket);
    this.modalSaidaAberto.set(true);
  }

  protected closeCheckoutModal(): void {
    this.modalSaidaAberto.set(false);
    this.veiculoSelecionado.set(null);
  }

  protected handleCheckoutConfirmed(): void {
    this.ticketsQuery.refetch();
    if (this.companyId() && this.companyId() !== 'null' && this.companyId() !== 'undefined') {
      this.reportQuery.refetch();
    }
  }
}