import { Component, Input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getVehicleColorHex } from '../../utils/colors';

@Component({
  selector: 'app-parking-spot-car',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="car-container">
      <svg viewBox="0 0 100 200" class="car-svg">
        <rect x="5" y="15" width="90" height="175" rx="35" fill="rgba(0,0,0,0.4)" filter="blur(4px)" />
        <rect x="8" y="10" width="84" height="180" rx="30" [style.fill]="fillColor()" />
        <rect x="18" y="45" width="64" height="90" rx="15" fill="#1e293b" />
        <path d="M 22 55 Q 50 35 78 55" stroke="#94a3b8" stroke-width="6" stroke-linecap="round" fill="none" />
        <path d="M 24 125 Q 50 140 76 125" stroke="#94a3b8" stroke-width="5" stroke-linecap="round" fill="none" />
        <rect x="18" y="15" width="12" height="6" rx="2" fill="#fef08a" />
        <rect x="70" y="15" width="12" height="6" rx="2" fill="#fef08a" />
        <rect x="18" y="179" width="12" height="6" rx="2" fill="#ef4444" />
        <rect x="70" y="179" width="12" height="6" rx="2" fill="#ef4444" />
      </svg>
    </div>
  `,
  styles: [`
    .car-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 4px;
    }
    .car-svg {
      width: 85%;
      height: 85%;
      max-height: 90px;
      transition: all 0.3s ease;
    }
  `]
})
export class ParkingSpotCar {
  @Input() color: string = '';

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color));
}
