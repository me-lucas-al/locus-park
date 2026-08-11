import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getVehicleColorHex } from '../../utils/colors';

@Component({
  selector: 'app-parking-spot-motorcycle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="motorcycle-container">
      <svg viewBox="0 0 100 200" class="motorcycle-svg">
        <defs>
          <linearGradient id="body3DGradientMoto" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#000000" stop-opacity="0.3" />
            <stop offset="12%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="50%" stop-color="#ffffff" stop-opacity="0.25" />
            <stop offset="88%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="100%" stop-color="#000000" stop-opacity="0.3" />
          </linearGradient>
          <linearGradient id="headlightMoto" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#ffffff" />
            <stop offset="100%" stop-color="#fef08a" />
          </linearGradient>
        </defs>

        <!-- Sombra externa para profundidade -->
        <rect x="30" y="14" width="40" height="172" rx="15" fill="rgba(0,0,0,0.45)" filter="blur(4px)" />

        <!-- Roda Dianteira -->
        <rect x="42" y="16" width="16" height="34" rx="4" fill="#090d16" />
        <rect x="44" y="20" width="8" height="26" fill="#1e293b" opacity="0.3" />

        <!-- Roda Traseira -->
        <rect x="38" y="150" width="24" height="38" rx="6" fill="#090d16" />
        <rect x="40" y="154" width="12" height="30" fill="#1e293b" opacity="0.3" />

        <!-- Suspensão / Garfo Dianteiro -->
        <path d="M 40,40 L 40,60 M 60,40 L 60,60" stroke="#94a3b8" stroke-width="3" fill="none" />

        <!-- Guidão -->
        <path d="M 24,70 Q 50,60 76,70" stroke="#090d16" stroke-width="4" stroke-linecap="round" fill="none" />
        <path d="M 26,70 Q 50,62 74,70" stroke="#cbd5e1" stroke-width="2" stroke-linecap="round" fill="none" />
        
        <!-- Retrovisores -->
        <circle cx="20" cy="65" r="4" fill="#090d16" />
        <circle cx="80" cy="65" r="4" fill="#090d16" />

        <!-- Corpo Principal (Tanque e Carenagem) -->
        <path d="M 36,65 C 36,55 64,55 64,65 L 68,110 C 68,130 32,130 32,110 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <path d="M 36,65 C 36,55 64,55 64,65 L 68,110 C 68,130 32,130 32,110 Z" fill="url(#body3DGradientMoto)" pointer-events="none" />
        
        <!-- Detalhes do Tanque -->
        <path d="M 45,70 Q 50,105 55,70" stroke="#ffffff" stroke-width="1.5" opacity="0.2" fill="none" />

        <!-- Assento -->
        <rect x="40" y="105" width="20" height="45" rx="8" fill="#111827" stroke="#090d16" stroke-width="2" />
        <!-- Detalhe do Assento -->
        <line x1="44" y1="125" x2="56" y2="125" stroke="#334155" stroke-width="1" />
        
        <!-- Motor / Carenagem Lateral -->
        <rect x="34" y="85" width="6" height="20" rx="2" fill="#1e293b" />
        <rect x="60" y="85" width="6" height="20" rx="2" fill="#1e293b" />

        <!-- Farol Dianteiro -->
        <circle cx="50" cy="48" r="8" fill="url(#headlightMoto)" stroke="#f59e0b" stroke-width="1" />

        <!-- Lanterna Traseira -->
        <rect x="44" y="180" width="12" height="6" rx="2" fill="#ef4444" />
        <rect x="46" y="182" width="8" height="2" fill="#ffffff" opacity="0.8" />
      </svg>
    </div>
  `,
  styles: [`
    .motorcycle-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 4px;
    }
    .motorcycle-svg {
      width: 80%;
      height: 95%;
      max-height: 95px;
      transition: all 0.3s ease;
    }
  `]
})
export class ParkingSpotMotorcycle {
  readonly color = input<string>('');

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color()));
}
