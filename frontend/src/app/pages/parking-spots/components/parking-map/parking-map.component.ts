import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
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

const SPOT_TARGET_WIDTH_PX = 72;
const SPOT_GAP_PX = 10;
const MIN_SPOTS_PER_ROW = 3;
const MAX_SPOTS_PER_ROW = 14;

function computeSpotsPerRow(containerWidth: number): number {
  if (containerWidth <= 0) return MAX_SPOTS_PER_ROW;
  const raw = Math.floor((containerWidth + SPOT_GAP_PX) / (SPOT_TARGET_WIDTH_PX + SPOT_GAP_PX));
  return Math.max(MIN_SPOTS_PER_ROW, Math.min(MAX_SPOTS_PER_ROW, raw));
}

@Component({
  selector: 'app-parking-map',
  standalone: true,
  imports: [CommonModule, ParkingSpotCar],
  templateUrl: './parking-map.component.html',
  styleUrl: './parking-map.component.css',
  host: { style: 'display: block; width: 100%;' },
})
export class ParkingMap implements AfterViewInit, OnDestroy {
  private readonly el = inject(ElementRef);
  private resizeObserver?: ResizeObserver;

  readonly spots = input<GridSpot[]>([]);
  readonly spotClick = output<GridSpot>();

  protected readonly containerWidth = signal<number>(0);

  protected readonly maxRowSpots = computed<number>(() =>
    computeSpotsPerRow(this.containerWidth())
  );

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

  ngAfterViewInit(): void {
    const initialWidth = this.el.nativeElement.offsetWidth;
    this.containerWidth.set(initialWidth);

    this.resizeObserver = new ResizeObserver((entries) => {
      const width = entries[0]?.contentRect.width ?? 0;
      if (width > 0) this.containerWidth.set(width);
    });

    this.resizeObserver.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    this.resizeObserver?.disconnect();
  }
}
