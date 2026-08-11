import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getVehicleColorHex } from '../../utils/colors';

@Component({
  selector: 'app-parking-spot-truck',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="truck-container">
      <svg viewBox="0 0 100 200" class="truck-svg">
        <defs>
          <linearGradient id="body3DGradientTruck" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#000000" stop-opacity="0.4" />
            <stop offset="15%" stop-color="#000000" stop-opacity="0.1" />
            <stop offset="50%" stop-color="#ffffff" stop-opacity="0.2" />
            <stop offset="85%" stop-color="#000000" stop-opacity="0.1" />
            <stop offset="100%" stop-color="#000000" stop-opacity="0.4" />
          </linearGradient>
          
          <linearGradient id="box3DGradient" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#334155" />
            <stop offset="20%" stop-color="#94a3b8" />
            <stop offset="50%" stop-color="#f8fafc" />
            <stop offset="80%" stop-color="#94a3b8" />
            <stop offset="100%" stop-color="#334155" />
          </linearGradient>

          <linearGradient id="glassGradientTruck" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#1e293b" />
            <stop offset="100%" stop-color="#0f172a" />
          </linearGradient>
          
          <linearGradient id="headlightGradientTruck" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#ffffff" />
            <stop offset="100%" stop-color="#fef08a" />
          </linearGradient>
        </defs>

        <!-- Sombra externa -->
        <rect x="0" y="8" width="100" height="186" rx="8" fill="rgba(0,0,0,0.55)" filter="blur(6px)" />

        <!-- Eixos / Pneus -->
        <!-- Eixo Dianteiro -->
        <rect x="2" y="24" width="8" height="24" rx="3" fill="#090d16" />
        <rect x="4" y="28" width="4" height="16" fill="#1e293b" opacity="0.4" />
        <rect x="90" y="24" width="8" height="24" rx="3" fill="#090d16" />
        <rect x="92" y="28" width="4" height="16" fill="#1e293b" opacity="0.4" />

        <!-- Eixos Traseiros (Duplos/Truck) -->
        <!-- Esquerda 1 -->
        <rect x="0" y="110" width="10" height="28" rx="4" fill="#090d16" />
        <rect x="2" y="114" width="6" height="20" fill="#1e293b" opacity="0.4" />
        <!-- Esquerda 2 -->
        <rect x="0" y="146" width="10" height="28" rx="4" fill="#090d16" />
        <rect x="2" y="150" width="6" height="20" fill="#1e293b" opacity="0.4" />
        <!-- Direita 1 -->
        <rect x="90" y="110" width="10" height="28" rx="4" fill="#090d16" />
        <rect x="92" y="114" width="6" height="20" fill="#1e293b" opacity="0.4" />
        <!-- Direita 2 -->
        <rect x="90" y="146" width="10" height="28" rx="4" fill="#090d16" />
        <rect x="92" y="150" width="6" height="20" fill="#1e293b" opacity="0.4" />

        <!-- ===== CABINE DO CAMINHÃO (Frontal) ===== -->
        <!-- Corpo da Cabine -->
        <rect x="8" y="10" width="84" height="42" rx="8" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <rect x="8" y="10" width="84" height="42" rx="8" fill="url(#body3DGradientTruck)" pointer-events="none" />
        
        <!-- Detalhe do Capô (Curto) -->
        <rect x="14" y="10" width="72" height="14" rx="4" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.3" />

        <!-- Grelha e Parachoque Dianteiro -->
        <rect x="26" y="8" width="48" height="6" rx="2" fill="#090d16" />
        <rect x="30" y="10" width="40" height="1" fill="#475569" />
        <rect x="30" y="12" width="40" height="1" fill="#475569" />

        <!-- Faróis Frontais Maiores e Duplos -->
        <rect x="12" y="10" width="12" height="8" rx="2" fill="url(#headlightGradientTruck)" stroke="#f59e0b" stroke-width="0.5" />
        <rect x="76" y="10" width="12" height="8" rx="2" fill="url(#headlightGradientTruck)" stroke="#f59e0b" stroke-width="0.5" />

        <!-- Retrovisores Largos (Braços longos de caminhão) -->
        <path d="M 8,30 L -2,30 L -2,42 L 2,42 L 2,34 L 8,34 Z" fill="#090d16" />
        <path d="M 92,30 L 102,30 L 102,42 L 98,42 L 98,34 L 92,34 Z" fill="#090d16" />

        <!-- Para-brisa Dianteiro (Reto e amplo) -->
        <path d="M 12,28 C 12,26 88,26 88,28 L 84,46 C 84,48 16,48 16,46 Z" fill="url(#glassGradientTruck)" />
        <rect x="20" y="32" width="60" height="10" rx="2" fill="#ffffff" opacity="0.1" />

        <!-- Teto da Cabine (Defletor de ar) -->
        <rect x="16" y="36" width="68" height="16" rx="4" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1" />
        <rect x="16" y="36" width="68" height="16" rx="4" fill="#000000" opacity="0.2" />

        <!-- ===== CARROCERIA / BAÚ (Traseira) ===== -->
        <!-- Base de conexão / Quinta roda -->
        <rect x="36" y="52" width="28" height="10" rx="2" fill="#1e293b" />
        <line x1="50" y1="52" x2="50" y2="60" stroke="#0f172a" stroke-width="4" />

        <!-- Caixa / Baú Principal -->
        <rect x="6" y="58" width="88" height="130" rx="2" fill="url(#box3DGradient)" stroke="#090d16" stroke-width="2" />
        
        <!-- Detalhes no teto do Baú (Ranhuras e estrutura reforçada) -->
        <line x1="8" y1="62" x2="92" y2="62" stroke="#94a3b8" stroke-width="1" />
        <line x1="8" y1="184" x2="92" y2="184" stroke="#94a3b8" stroke-width="1" />
        <!-- Linhas longitudinais do teto do baú -->
        <line x1="20" y1="60" x2="20" y2="186" stroke="#94a3b8" stroke-width="1.5" />
        <line x1="35" y1="60" x2="35" y2="186" stroke="#94a3b8" stroke-width="1.5" />
        <line x1="50" y1="60" x2="50" y2="186" stroke="#94a3b8" stroke-width="1.5" />
        <line x1="65" y1="60" x2="65" y2="186" stroke="#94a3b8" stroke-width="1.5" />
        <line x1="80" y1="60" x2="80" y2="186" stroke="#94a3b8" stroke-width="1.5" />

        <!-- Parachoque Traseiro -->
        <rect x="10" y="188" width="80" height="6" rx="1" fill="#090d16" />
        
        <!-- Lanternas Traseiras -->
        <rect x="14" y="188" width="12" height="4" rx="1" fill="#ef4444" />
        <rect x="16" y="189" width="4" height="2" fill="#ffffff" opacity="0.8" />
        <rect x="74" y="188" width="12" height="4" rx="1" fill="#ef4444" />
        <rect x="80" y="189" width="4" height="2" fill="#ffffff" opacity="0.8" />
      </svg>
    </div>
  `,
  styles: [`
    .truck-container {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2px;
    }
    .truck-svg {
      width: 95%;
      height: 98%;
      max-height: 100px;
      transition: all 0.3s ease;
    }
  `]
})
export class ParkingSpotTruck {
  readonly color = input<string>('');

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color()));
}
