import { Component, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketResponse } from '../../../../core/domains/ticket/ticket.types';
import { ParkingSpotCar } from '../parking-spot-car/parking-spot-car.component';

interface GridSpot {
  number: number;
  ticket: TicketResponse | null;
  status: 'Livre' | 'Ocupada';
}

interface Street {
  topSpots: GridSpot[];
  bottomSpots: GridSpot[];
}

@Component({
  selector: 'app-parking-map',
  standalone: true,
  imports: [CommonModule, ParkingSpotCar],
  templateUrl: './parking-map.component.html',
  styleUrl: './parking-map.component.css'
})
export class ParkingMap {
  readonly spots = input<GridSpot[]>([]);
  readonly spotClick = output<GridSpot>();

  protected readonly streets = computed<Street[]>(() => {
    const list = this.spots();
    const result: Street[] = [];
    const chunkSize = 28;
    const maxRowSpots = 14;

    for (let i = 0; i < list.length; i += chunkSize) {
      const chunk = list.slice(i, i + chunkSize);
      result.push({
        topSpots: chunk.slice(0, maxRowSpots),
        bottomSpots: chunk.slice(maxRowSpots)
      });
    }

    return result;
  });
}
