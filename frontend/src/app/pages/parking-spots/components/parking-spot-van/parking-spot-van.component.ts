import { Component, Input, computed } from '@angular/core';
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
        <rect x="6" y="8" width="88" height="186" rx="18" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <!-- Camada de volume 3D -->
        <rect x="6" y="8" width="88" height="186" rx="18" fill="url(#body3DGradientVan)" pointer-events="none" />
        
        <!-- Detalhes do Capô (Curto) -->
        <path d="M 18,8 L 22,26 C 28,30 72,30 78,26 L 82,8" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.3" />

        <!-- Retrovisores Laterais (Maiores) -->
        <!-- Esquerdo -->
        <path d="M 6,48 C -2,48 -4,52 -4,60 C -4,66 2,68 6,64 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />
        <!-- Direito -->
        <path d="M 94,48 C 102,48 104,52 104,60 C 104,66 98,68 94,64 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />

        <!-- Sombra projetada da Cabine para efeito 3D -->
        <rect x="14" y="32" width="72" height="152" rx="12" fill="#000000" opacity="0.3" filter="blur(3px)" />

        <!-- Teto / Área principal superior -->
        <rect x="16" y="30" width="68" height="152" rx="10" fill="#111827" stroke="#090d16" stroke-width="2" />
        
        <!-- Para-brisa Dianteiro (Grande) -->
        <path d="M 18,34 C 26,26 74,26 82,34 L 78,54 C 68,48 32,48 22,54 Z" fill="url(#glassGradientVan)" />
        <path d="M 18,34 C 26,26 74,26 82,34 L 78,54 C 68,48 32,48 22,54 Z" fill="url(#reflectGradientVan)" />
        
        <!-- Para-brisa Traseiro (Reto) -->
        <rect x="22" y="170" width="56" height="8" rx="2" fill="url(#glassGradientVan)" />
        <rect x="22" y="170" width="56" height="8" rx="2" fill="url(#reflectGradientVan)" />

        <!-- Vidros Laterais (Múltiplos para van de passageiros ou painel longo) -->
        <!-- Lado Esquerdo -->
        <rect x="18" y="58" width="5" height="30" fill="url(#glassGradientVan)" />
        <rect x="18" y="92" width="5" height="36" fill="url(#glassGradientVan)" />
        <rect x="18" y="132" width="5" height="34" fill="url(#glassGradientVan)" />
        <!-- Lado Direito -->
        <rect x="77" y="58" width="5" height="30" fill="url(#glassGradientVan)" />
        <rect x="77" y="92" width="5" height="36" fill="url(#glassGradientVan)" />
        <rect x="77" y="132" width="5" height="34" fill="url(#glassGradientVan)" />
        
        <!-- Detalhes do Teto -->
        <rect x="30" y="60" width="40" height="100" rx="4" fill="#090d16" opacity="0.2" />
        <!-- Ranhuras no teto para reforço estrutural comum em vans -->
        <line x1="40" y1="65" x2="40" y2="150" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />
        <line x1="50" y1="65" x2="50" y2="150" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />
        <line x1="60" y1="65" x2="60" y2="150" stroke="#ffffff" stroke-width="1.5" opacity="0.1" />

        <!-- Faróis Dianteiros -->
        <rect x="12" y="10" width="16" height="10" rx="3" fill="url(#headlightGradientVan)" stroke="#f59e0b" stroke-width="0.5" />
        <rect x="72" y="10" width="16" height="10" rx="3" fill="url(#headlightGradientVan)" stroke="#f59e0b" stroke-width="0.5" />

        <!-- Grelha frontal (Grande) -->
        <rect x="34" y="9" width="32" height="6" rx="1" fill="#090d16" />
        <rect x="34" y="11" width="32" height="1" fill="#1e293b" />
        <rect x="34" y="13" width="32" height="1" fill="#1e293b" />
        <circle cx="50" cy="12" r="1.5" fill="#e2e8f0" opacity="0.9" />

        <!-- Lanternas Traseiras (Verticais) -->
        <rect x="10" y="180" width="6" height="12" rx="2" fill="#ef4444" />
        <rect x="11" y="182" width="4" height="4" fill="#ffffff" opacity="0.8" />
        <rect x="84" y="180" width="6" height="12" rx="2" fill="#ef4444" />
        <rect x="85" y="182" width="4" height="4" fill="#ffffff" opacity="0.8" />
        
        <!-- Detalhes da Porta Traseira (Dupla) -->
        <line x1="50" y1="182" x2="50" y2="194" stroke="#090d16" stroke-width="1.5" opacity="0.4" />
        <rect x="46" y="186" width="3" height="4" rx="1" fill="#090d16" opacity="0.6" />
        <rect x="51" y="186" width="3" height="4" rx="1" fill="#090d16" opacity="0.6" />
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
  @Input() color: string = '';

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color));
}
