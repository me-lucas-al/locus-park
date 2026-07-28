import { Injectable } from '@angular/core';
import { TicketResponse } from '../../core/domains/ticket/ticket.types';

@Injectable({ providedIn: 'root' })
export class SpotAssignmentService {
  private readonly STORAGE_KEY = 'locus_park_spot_assignments';

  private getAssignments(): Record<string, number> {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    if (!raw) return {};
    try {
      return JSON.parse(raw);
    } catch {
      return {};
    }
  }

  private saveAssignments(assignments: Record<string, number>): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(assignments));
  }

  getSpot(ticket: TicketResponse): number {
    // Se o ticket já tiver exitedAt (finalizado) ou não estiver ATIVO, não possui vaga ativa
    if (ticket.exitedAt || ticket.status === 'PAID') {
      return 0;
    }

    const assignments = this.getAssignments();
    
    // Se já tiver vaga atribuída no storage, retorna
    if (assignments[ticket.id]) {
      return assignments[ticket.id];
    }

    // Achar uma vaga disponível de 1 a 120
    const occupiedSpots = new Set(Object.values(assignments));
    let chosenSpot = 1;
    for (let i = 1; i <= 120; i++) {
      if (!occupiedSpots.has(i)) {
        chosenSpot = i;
        break;
      }
    }

    // Salvar atribuição
    assignments[ticket.id] = chosenSpot;
    this.saveAssignments(assignments);

    return chosenSpot;
  }

  assignSpot(ticketId: string, spotNumber: number): void {
    const assignments = this.getAssignments();
    assignments[ticketId] = spotNumber;
    this.saveAssignments(assignments);
  }

  releaseSpot(ticketId: string): void {
    const assignments = this.getAssignments();
    delete assignments[ticketId];
    this.saveAssignments(assignments);
  }

  cleanInactiveTickets(activeTickets: TicketResponse[]): void {
    const assignments = this.getAssignments();
    const activeIds = new Set(
      activeTickets.filter((t) => !t.exitedAt && t.status === 'ACTIVE').map((t) => t.id),
    );
    
    let changed = false;
    Object.keys(assignments).forEach((id) => {
      if (!activeIds.has(id)) {
        delete assignments[id];
        changed = true;
      }
    });

    if (changed) {
      this.saveAssignments(assignments);
    }
  }
}
