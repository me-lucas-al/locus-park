import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getVehicleColorHex } from '../../utils/colors';

@Component({
  selector: 'app-parking-spot-van',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="van-container">
      <svg viewBox="0 0 100 200" class="van-svg">
        <defs>
          <linearGradient id="glassGradientVan" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#334155" />
            <stop offset="100%" stop-color="#0f172a" />
          </linearGradient>
          <linearGradient id="reflectGradientVan" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="#ffffff" stop-opacity="0.25" />
            <stop offset="30%" stop-color="#ffffff" stop-opacity="0.05" />
            <stop offset="31%" stop-color="#ffffff" stop-opacity="0" />
            <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="headlightGradientVan" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#ffffff" />
            <stop offset="100%" stop-color="#fef08a" />
          </linearGradient>
          <linearGradient id="body3DGradientVan" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#000000" stop-opacity="0.35" />
            <stop offset="12%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="50%" stop-color="#ffffff" stop-opacity="0.15" />
            <stop offset="88%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="100%" stop-color="#000000" stop-opacity="0.35" />
          </linearGradient>
        </defs>

        <!-- Sombra externa para profundidade -->
        <rect x="2" y="10" width="96" height="184" rx="20" fill="rgba(0,0,0,0.5)" filter="blur(6px)" />
        
        <!-- Rodas/Pneus -->
        <!-- Dianteira Esquerda -->
        <rect x="0" y="32" width="10" height="32" rx="4" fill="#090d16" />
        <rect x="2" y="36" width="6" height="24" fill="#1e293b" opacity="0.3" />
        <!-- Dianteira Direita -->
        <rect x="90" y="32" width="10" height="32" rx="4" fill="#090d16" />
        <rect x="92" y="36" width="6" height="24" fill="#1e293b" opacity="0.3" />
        <!-- Traseira Esquerda -->
        <rect x="0" y="140" width="10" height="34" rx="4" fill="#090d16" />
        <rect x="2" y="144" width="6" height="26" fill="#1e293b" opacity="0.3" />
        <!-- Traseira Direita -->
        <rect x="90" y="140" width="10" height="34" rx="4" fill="#090d16" />
        <rect x="92" y="144" width="6" height="26" fill="#1e293b" opacity="0.3" />

        <!-- Corpo Principal da Van -->
        <rect x="6" y="10" width="88" height="184" rx="14" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <!-- Camada de volume 3D -->
        <rect x="6" y="10" width="88" height="184" rx="14" fill="url(#body3DGradientVan)" pointer-events="none" />
        
        <!-- Detalhes do Capô (Arredondado e inclinado) -->
        <path d="M 16,10 L 20,28 C 30,34 70,34 80,28 L 84,10" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.3" />

        <!-- Retrovisores Laterais -->
        <!-- Esquerdo -->
        <path d="M 6,50 C -2,50 -4,54 -4,62 C -4,68 2,70 6,66 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />
        <!-- Direito -->
        <path d="M 94,50 C 102,50 104,54 104,62 C 104,68 98,70 94,66 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />

        <!-- Sombra projetada da Cabine -->
        <rect x="12" y="32" width="76" height="154" rx="8" fill="#000000" opacity="0.35" filter="blur(3px)" />

        <!-- Teto / Cabine Principal -->
        <rect x="14" y="30" width="72" height="154" rx="8" fill="#111827" stroke="#090d16" stroke-width="2" />
        
        <!-- Para-brisa Dianteiro (Mais curvo na base) -->
        <path d="M 16,34 C 24,28 76,28 84,34 L 80,52 C 70,48 30,48 20,52 Z" fill="url(#glassGradientVan)" />
        <path d="M 16,34 C 24,28 76,28 84,34 L 80,52 C 70,48 30,48 20,52 Z" fill="url(#reflectGradientVan)" />
        
        <!-- Para-brisa Traseiro -->
        <path d="M 18,178 C 30,182 70,182 82,178 L 78,168 C 65,172 35,172 22,168 Z" fill="url(#glassGradientVan)" />
        <path d="M 18,178 C 30,182 70,182 82,178 L 78,168 C 65,172 35,172 22,168 Z" fill="url(#reflectGradientVan)" />

        <!-- Vidros Laterais (Painéis contínuos) -->
        <!-- Lado Esquerdo -->
        <rect x="16" y="56" width="5" height="106" rx="2" fill="url(#glassGradientVan)" />
        <!-- Lado Direito -->
        <rect x="79" y="56" width="5" height="106" rx="2" fill="url(#glassGradientVan)" />
        
        <!-- Detalhes do Teto -->
        <rect x="30" y="58" width="40" height="110" rx="4" fill="#090d16" opacity="0.2" />
        <!-- Ranhuras do teto -->
        <line x1="40" y1="65" x2="40" y2="160" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />
        <line x1="50" y1="65" x2="50" y2="160" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />
        <line x1="60" y1="65" x2="60" y2="160" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />

        <!-- Faróis Dianteiros (Com cantos arredondados) -->
        <rect x="10" y="12" width="16" height="10" rx="4" fill="url(#headlightGradientVan)" stroke="#f59e0b" stroke-width="0.5" />
        <rect x="74" y="12" width="16" height="10" rx="4" fill="url(#headlightGradientVan)" stroke="#f59e0b" stroke-width="0.5" />

        <!-- Grelha Frontal -->
        <rect x="34" y="12" width="32" height="6" rx="2" fill="#090d16" />
        <circle cx="50" cy="15" r="1.5" fill="#e2e8f0" opacity="0.9" />

        <!-- Lanternas Traseiras -->
        <rect x="10" y="186" width="8" height="8" rx="2" fill="#ef4444" />
        <rect x="82" y="186" width="8" height="8" rx="2" fill="#ef4444" />
        
        <!-- Detalhes da Porta Traseira (Dupla) -->
        <line x1="50" y1="184" x2="50" y2="194" stroke="#090d16" stroke-width="1.5" opacity="0.4" />
        <rect x="46" y="188" width="3" height="2" rx="0.5" fill="#090d16" opacity="0.8" />
        <rect x="51" y="188" width="3" height="2" rx="0.5" fill="#090d16" opacity="0.8" />
      </svg>
    </div>
  `,
  styles: [`
    .van-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2px;
    }
    .van-svg {
      width: 90%;
      height: 90%;
      max-height: 95px;
      transition: all 0.3s ease;
    }
  `]
})
export class ParkingSpotVan {
  readonly color = input<string>('');

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color()));
}
