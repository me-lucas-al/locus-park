import { Component, input, output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketResponse } from '../../../../core/domains/ticket/ticket.types';
import { SpotAssignmentService } from '../../../../shared/services/spot-assignment.service';

@Component({
  selector: 'app-occupied-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './occupied-list.component.html',
  styleUrl: './occupied-list.component.css',
})
export class OccupiedListComponent {
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  readonly tickets = input.required<TicketResponse[]>();
  readonly releaseClick = output<TicketResponse>();

  protected getSpotNumber(ticket: TicketResponse): number {
    return this.spotAssignmentService.getSpot(ticket);
  }

  protected onReleaseClick(ticket: TicketResponse): void {
    this.releaseClick.emit(ticket);
  }
}
