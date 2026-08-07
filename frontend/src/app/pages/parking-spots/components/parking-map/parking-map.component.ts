import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
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

const BREAKPOINTS = [
  { minWidth: 1440, spotsPerRow: 14 },
  { minWidth: 1024, spotsPerRow: 10 },
  { minWidth: 768, spotsPerRow: 7 },
  { minWidth: 480, spotsPerRow: 5 },
  { minWidth: 0, spotsPerRow: 4 },
] as const;

@Component({
  selector: 'app-parking-map',
  standalone: true,
  imports: [CommonModule, ParkingSpotCar],
  templateUrl: './parking-map.component.html',
  styleUrl: './parking-map.component.css',
})
export class ParkingMap implements OnInit, OnDestroy {
  private readonly el = inject(ElementRef);
  private resizeObserver?: ResizeObserver;

  readonly spots = input<GridSpot[]>([]);
  readonly spotClick = output<GridSpot>();

  protected readonly containerWidth = signal<number>(1440);

  protected readonly maxRowSpots = computed<number>(() => {
    const width = this.containerWidth();
    for (const bp of BREAKPOINTS) {
      if (width >= bp.minWidth) return bp.spotsPerRow;
    }
    return 4;
  });

  protected readonly streets = computed<Street[]>(() => {
    const list = this.spots();
    const spotsPerRow = this.maxRowSpots();
    const chunkSize = spotsPerRow * 2;
    const result: Street[] = [];

    for (let i = 0; i < list.length; i += chunkSize) {
      const chunk = list.slice(i, i + chunkSize);
      result.push({
        topSpots: chunk.slice(0, spotsPerRow),
        bottomSpots: chunk.slice(spotsPerRow),
      });
    }

    return result;
  });

  ngOnInit(): void {
    this.resizeObserver = new ResizeObserver((entries) => {
      const width = entries[0]?.contentRect.width ?? this.el.nativeElement.offsetWidth;
      this.containerWidth.set(width);
    });
    this.resizeObserver.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }
}
