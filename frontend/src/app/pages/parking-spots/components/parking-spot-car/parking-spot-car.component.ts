import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { getVehicleColorHex } from '../../utils/colors';

@Component({
  selector: 'app-parking-spot-car',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="car-container">
      <svg viewBox="0 0 100 200" class="car-svg">
        <!-- Definições para efeitos visuais do carro -->
        <defs>
          <linearGradient id="glassGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#334155" />
            <stop offset="100%" stop-color="#0f172a" />
          </linearGradient>
          <linearGradient id="reflectGradient" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="#ffffff" stop-opacity="0.25" />
            <stop offset="30%" stop-color="#ffffff" stop-opacity="0.05" />
            <stop offset="31%" stop-color="#ffffff" stop-opacity="0" />
            <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="headlightGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#ffffff" />
            <stop offset="100%" stop-color="#fef08a" />
          </linearGradient>
          <!-- Gradiente 3D para dar volume ao corpo do carro -->
          <linearGradient id="body3DGradient" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="#000000" stop-opacity="0.3" />
            <stop offset="12%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="50%" stop-color="#ffffff" stop-opacity="0.18" />
            <stop offset="88%" stop-color="#000000" stop-opacity="0.05" />
            <stop offset="100%" stop-color="#000000" stop-opacity="0.3" />
          </linearGradient>
        </defs>

        <!-- Sombra externa para profundidade (Ground Shadow) -->
        <rect x="4" y="14" width="92" height="176" rx="34" fill="rgba(0,0,0,0.45)" filter="blur(5px)" />
        
        <!-- Rodas/Pneus (Wheels) -->
        <!-- Dianteira Esquerda -->
        <rect x="2" y="36" width="8" height="28" rx="4" fill="#090d16" />
        <rect x="4" y="40" width="4" height="20" fill="#1e293b" opacity="0.3" />
        <!-- Dianteira Direita -->
        <rect x="90" y="36" width="8" height="28" rx="4" fill="#090d16" />
        <rect x="92" y="40" width="4" height="20" fill="#1e293b" opacity="0.3" />
        <!-- Traseira Esquerda -->
        <rect x="2" y="136" width="8" height="30" rx="4" fill="#090d16" />
        <rect x="4" y="140" width="4" height="22" fill="#1e293b" opacity="0.3" />
        <!-- Traseira Direita -->
        <rect x="90" y="136" width="8" height="30" rx="4" fill="#090d16" />
        <rect x="92" y="140" width="4" height="22" fill="#1e293b" opacity="0.3" />

        <!-- Corpo Principal do Carro (Main Body) -->
        <rect x="8" y="10" width="84" height="180" rx="30" [style.fill]="fillColor()" stroke="#090d16" stroke-width="2" />
        <!-- Camada de volume 3D sobre o chassi -->
        <rect x="8" y="10" width="84" height="180" rx="30" fill="url(#body3DGradient)" pointer-events="none" />
        
        <!-- Detalhes do Capô (Lines) -->
        <path d="M 22,12 L 28,32 C 32,38 68,38 72,32 L 78,12" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.2" />
        <path d="M 36,12 L 38,28 Q 50,32 62,28 L 64,12" fill="none" stroke="#ffffff" stroke-width="1.2" opacity="0.15" />

        <!-- Retrovisores Laterais -->
        <!-- Esquerdo -->
        <path d="M 8,60 C 2,60 -1,63 -1,70 C -1,75 3,77 8,73 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />
        <!-- Direito -->
        <path d="M 92,60 C 98,60 101,63 101,70 C 101,75 97,77 92,73 Z" [style.fill]="fillColor()" stroke="#090d16" stroke-width="1.5" />

        <!-- Sombra projetada da Cabine para efeito 3D de elevação -->
        <rect x="16" y="47" width="68" height="92" rx="18" fill="#000000" opacity="0.4" filter="blur(2.5px)" />

        <!-- Área da Cabine (Cabin Area) -->
        <rect x="18" y="44" width="64" height="92" rx="16" fill="#111827" stroke="#090d16" stroke-width="2" />
        
        <!-- Vidros -->
        <!-- Para-brisa Dianteiro -->
        <path d="M 22,48 C 30,41 70,41 78,48 L 74,66 C 65,60 35,60 26,66 Z" fill="url(#glassGradient)" />
        <path d="M 22,48 C 30,41 70,41 78,48 L 74,66 C 65,60 35,60 26,66 Z" fill="url(#reflectGradient)" />
        
        <!-- Para-brisa Traseiro -->
        <path d="M 24,126 C 30,133 70,133 76,126 L 72,112 C 65,116 35,116 28,112 Z" fill="url(#glassGradient)" />
        <path d="M 24,126 C 30,133 70,133 76,126 L 72,112 C 65,116 35,116 28,112 Z" fill="url(#reflectGradient)" />

        <!-- Vidros Laterais -->
        <path d="M 20,54 L 24,58 L 24,82 L 20,86 Z" fill="url(#glassGradient)" />
        <path d="M 20,91 L 24,94 L 24,118 L 20,121 Z" fill="url(#glassGradient)" />
        <path d="M 80,54 L 76,58 L 76,82 L 80,86 Z" fill="url(#glassGradient)" />
        <path d="M 80,91 L 76,94 L 76,118 L 80,121 Z" fill="url(#glassGradient)" />
        
        <!-- Teto Solar / Detalhes de Teto -->
        <rect x="28" y="70" width="44" height="34" rx="6" fill="#090d16" opacity="0.3" />
        <path d="M 50,105 L 50,96 C 50,93 49,94 51,94" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.15" />
        <path d="M 50,105 L 50,96 C 50,93 49,94 51,94" stroke="#000000" stroke-width="2" stroke-linecap="round" fill="none" opacity="0.4" />

        <!-- Faróis Dianteiros (Sem feixe de brilho elíptico) -->
        <!-- Farol Esquerdo -->
        <path d="M 12,12 C 16,12 24,15 23,22 C 18,22 12,18 12,12 Z" fill="url(#headlightGradient)" stroke="#f59e0b" stroke-width="0.5" />
        
        <!-- Farol Direito -->
        <path d="M 88,12 C 84,12 76,15 77,22 C 82,22 88,18 88,12 Z" fill="url(#headlightGradient)" stroke="#f59e0b" stroke-width="0.5" />

        <!-- Grelha frontal e Logo -->
        <rect x="42" y="11" width="16" height="4" rx="1" fill="#090d16" />
        <circle cx="50" cy="13" r="1.5" fill="#e2e8f0" opacity="0.8" />

        <!-- Lanternas Traseiras -->
        <!-- Esquerda -->
        <path d="M 12,184 C 16,184 24,181 22,176 C 18,176 12,179 12,184 Z" fill="#ef4444" />
        <rect x="14" y="179" width="4" height="2" fill="#ffffff" opacity="0.8" />
        <!-- Direita -->
        <path d="M 88,184 C 84,184 76,181 78,176 C 82,176 88,179 88,184 Z" fill="#ef4444" />
        <rect x="82" y="179" width="4" height="2" fill="#ffffff" opacity="0.8" />
        
        <!-- Detalhes do Porta-malas -->
        <path d="M 24,188 L 28,170 Q 50,166 72,170 L 76,188" fill="none" stroke="#090d16" stroke-width="1.5" opacity="0.2" />
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
  readonly color = input<string>('');

  protected readonly fillColor = computed(() => getVehicleColorHex(this.color()));
}
