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
        <!-- Sombra Projetada Cavalo Mecanico -->
        <rect x="20" y="14" width="60" height="42" rx="6" fill="#000000" opacity="0.6" filter="blur(4px)" />

        <!-- Corpo da Cabine (Cavalo Mecânico bem mais estreito) -->
        <rect x="22" y="10" width="56" height="42" rx="6" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <rect x="22" y="10" width="56" height="42" rx="6" fill="url(#body3DGradientTruck)" pointer-events="none" />
        
        <!-- Detalhe do Capô (Curto) -->
        <rect x="26" y="10" width="48" height="10" rx="2" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.3" />

        <!-- Grelha e Parachoque Dianteiro -->
        <rect x="36" y="8" width="28" height="6" rx="2" fill="#090d16" />
        <rect x="38" y="10" width="24" height="1" fill="#475569" />
        <rect x="38" y="12" width="24" height="1" fill="#475569" />

        <!-- Faróis Frontais -->
        <rect x="24" y="10" width="10" height="6" rx="2" fill="url(#headlightGradientTruck)" stroke="#f59e0b" stroke-width="0.5" />
        <rect x="66" y="10" width="10" height="6" rx="2" fill="url(#headlightGradientTruck)" stroke="#f59e0b" stroke-width="0.5" />

        <!-- Retrovisores Largos (Braços estendidos pra fora da cabine) -->
        <path d="M 22,26 L 12,26 L 12,38 L 16,38 L 16,30 L 22,30 Z" fill="#090d16" />
        <path d="M 78,26 L 88,26 L 88,38 L 84,38 L 84,30 L 78,30 Z" fill="#090d16" />

        <!-- Para-brisa Dianteiro -->
        <path d="M 24,24 C 24,22 76,22 76,24 L 74,40 C 74,42 26,42 26,40 Z" fill="url(#glassGradientTruck)" />
        <rect x="28" y="28" width="44" height="6" rx="1" fill="#ffffff" opacity="0.15" />

        <!-- Teto da Cabine (Defletor aerodinâmico bem acentuado) -->
        <rect x="26" y="32" width="48" height="18" rx="4" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />
        <rect x="26" y="32" width="48" height="18" rx="4" fill="#000000" opacity="0.2" />
        <!-- Escapamento Vertical Atrás da Cabine -->
        <circle cx="20" cy="54" r="3" fill="#64748b" stroke="#090d16" stroke-width="1" />
        <circle cx="80" cy="54" r="3" fill="#64748b" stroke="#090d16" stroke-width="1" />

        <!-- ===== CARROCERIA / BAÚ (Traseira) ===== -->
        <!-- Chassi Exposto (Pescoço do caminhão) -->
        <rect x="36" y="52" width="28" height="14" rx="1" fill="#090d16" />
        <!-- Base de conexão / Quinta roda -->
        <rect x="42" y="54" width="16" height="10" rx="2" fill="#1e293b" />

        <!-- Sombra Projetada pelo Baú -->
        <rect x="0" y="66" width="100" height="122" rx="4" fill="#000000" opacity="0.35" filter="blur(5px)" />

        <!-- Caixa / Baú Principal -->
        <rect x="2" y="64" width="96" height="124" rx="1" fill="url(#box3DGradient)" stroke="#090d16" stroke-width="2.5" />
        
        <!-- Detalhes no teto do Baú (Ranhuras transversais tipo container/furgão pesado) -->
        <!-- Linhas transversais -->
        <line x1="4" y1="74" x2="96" y2="74" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="94" x2="96" y2="94" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="114" x2="96" y2="114" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="134" x2="96" y2="134" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="154" x2="96" y2="154" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="174" x2="96" y2="174" stroke="#94a3b8" stroke-width="2" />
        <line x1="4" y1="182" x2="96" y2="182" stroke="#94a3b8" stroke-width="1" />

        <!-- Parachoque Traseiro Robusto -->
        <rect x="12" y="188" width="76" height="8" rx="2" fill="#090d16" />
        <!-- Placa e detalhes metálicos -->
        <rect x="44" y="188" width="12" height="4" fill="#e2e8f0" />
        
        <!-- Lanternas Traseiras Quadradas -->
        <rect x="16" y="190" width="12" height="4" fill="#ef4444" />
        <rect x="72" y="190" width="12" height="4" fill="#ef4444" />
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
