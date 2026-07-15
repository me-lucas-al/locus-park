import { Component, input, output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketResponse } from '../../../../core/domains/ticket/ticket.types';
import { SpotAssignmentService } from '../../../../shared/services/spot-assignment.service';
import { GridSpot, LayoutItem } from '../../dashboard.types';

@Component({
  selector: 'app-parking-grid',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parking-grid.component.html',
  styleUrl: './parking-grid.component.css',
})
export class ParkingGridComponent {
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  readonly tickets = input.required<TicketResponse[]>();
  readonly totalSpots = input.required<number>();
  readonly spotClick = output<TicketResponse>();

  readonly spotsPerRow = computed<number>(() => {
    const total = this.totalSpots();
    if (total <= 15) return 5;
    if (total <= 40) return 8;
    if (total <= 80) return 10;
    return 12;
  });

  readonly gridSpots = computed<GridSpot[]>(() => {
    const activeTickets = this.tickets();
    this.spotAssignmentService.cleanInactiveTickets(activeTickets);

    const ticketMap = new Map<number, TicketResponse>();
    activeTickets.forEach((ticket) => {
      const spot = this.spotAssignmentService.getSpot(ticket);
      if (spot) {
        ticketMap.set(spot, ticket);
      }
    });

    const spots: GridSpot[] = [];
    const total = this.totalSpots();
    for (let i = 1; i <= total; i++) {
      const ticket = ticketMap.get(i) || null;
      spots.push({
        number: i,
        ticket,
        status: ticket ? 'Ocupada' : 'Livre',
      });
    }
    return spots;
  });

  readonly layoutItems = computed<LayoutItem[]>(() => {
    const spots = this.gridSpots();
    const spotsPerRow = this.spotsPerRow();
    const items: LayoutItem[] = [];

    items.push({
      type: 'corridor',
      direction: 'right',
      corridorIndex: 0,
    });

    let i = 0;
    let fileiraCountInSet = 0;
    let spotRowIndex = 0;

    while (i < spots.length) {
      const rowSpots = spots.slice(i, i + spotsPerRow);
      i += spotsPerRow;
      spotRowIndex++;

      const orientation = spotRowIndex % 2 !== 0 ? 'top-open' : 'bottom-open';

      rowSpots.forEach((spot) => {
        items.push({
          type: 'spot',
          spot,
          spotRowIndex,
          orientation,
        });
      });

      if (rowSpots.length < spotsPerRow) {
        const emptyCount = spotsPerRow - rowSpots.length;
        for (let e = 0; e < emptyCount; e++) {
          items.push({
            type: 'empty',
          });
        }
      }

      fileiraCountInSet++;

      if (fileiraCountInSet === 2 && i < spots.length) {
        const corridorsCount = items.filter((item) => item.type === 'corridor').length;
        items.push({
          type: 'corridor',
          direction: corridorsCount % 2 === 0 ? 'right' : 'left',
          corridorIndex: corridorsCount,
        });
        fileiraCountInSet = 0;
      }
    }

    if (items.length > 0 && items[items.length - 1].type !== 'corridor') {
      const corridorsCount = items.filter((item) => item.type === 'corridor').length;
      items.push({
        type: 'corridor',
        direction: corridorsCount % 2 === 0 ? 'right' : 'left',
        corridorIndex: corridorsCount,
      });
    }

    return items;
  });

  protected getCarColorHex(ticket: TicketResponse | null): string {
    if (!ticket || !ticket.vehicle) return '#FFFFFF';
    const colorName = ticket.vehicle.color?.toLowerCase().trim();
    if (!colorName) return '#FFFFFF';
    
    if (colorName.startsWith('#')) return colorName;
    
    const colorsMap: Record<string, string> = {
      branco: '#FFFFFF',
      creme: '#FFFDD0',
      prata: '#C0C0C0',
      cinza: '#708090',
      grafite: '#4F5D65',
      chumbo: '#374151',
      preto: '#1C1C1C',
      vermelho: '#D32F2F',
      vinho: '#58111A',
      rosa: '#FF69B4',
      roxo: '#4B0082',
      azul_claro: '#7EC8E3',
      azul_royal: '#0040FF',
      azul: '#1C3B57',
      ciano: '#00A896',
      verde_claro: '#A3E635',
      verde: '#1B4D3E',
      verde_militar: '#4B5320',
      bege: '#F5F5DC',
      champanhe: '#EEDC82',
      dourado: '#D4AF37',
      bronze: '#A87C43',
      marrom: '#5C4033',
      amarelo: '#F9A602',
      laranja: '#E65C00',
      outro: '#5A5A5A'
    };
    
    if (colorsMap[colorName]) {
      return colorsMap[colorName];
    }
    
    const plate = ticket.vehicle.plate || '';
    if (plate && plate !== 'N/A') {
      const availableColors = [
        '#FFFFFF', '#C0C0C0', '#708090', '#1C1C1C', '#D32F2F', 
        '#1C3B57', '#1B4D3E', '#F5F5DC', '#A87C43', '#5C4033'
      ];
      let hash = 0;
      for (let i = 0; i < plate.length; i++) {
        hash = plate.charCodeAt(i) + ((hash << 5) - hash);
      }
      const index = Math.abs(hash) % availableColors.length;
      return availableColors[index];
    }
    
    return '#5A5A5A';
  }

  protected onSpotClick(spot: GridSpot): void {
    if (spot.status === 'Ocupada' && spot.ticket) {
      this.spotClick.emit(spot.ticket);
    }
  }
}
