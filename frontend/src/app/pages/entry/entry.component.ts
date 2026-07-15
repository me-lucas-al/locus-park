import { Component, OnInit, OnDestroy, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { useTicketsQuery, useCheckInMutation } from '../../core/domains/ticket/ticket.hooks';
import { useCreateVehicleMutation } from '../../core/domains/vehicle/vehicle.hooks';
import { useUserProfileQuery } from '../../core/domains/user/user.hooks';
import { ToastService } from '../../shared/services/toast.service';
import { SpotAssignmentService } from '../../shared/services/spot-assignment.service';

interface VagaOption {
  numero: number;
  label: string;
}

import { LoadingDirective } from '../../shared/directives/loading.directive';

@Component({
  selector: 'app-entry',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective],
  templateUrl: './entry.component.html',
  styleUrl: './entry.component.css',
})
export class Entry implements OnInit, OnDestroy {
  private readonly toastService = inject(ToastService);
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  // Queries
  protected readonly ticketsQuery = useTicketsQuery();
  protected readonly profileQuery = useUserProfileQuery();

  // Signals
  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || '');

  // Mutações
  protected readonly checkInMutation = useCheckInMutation();
  protected readonly createVehicleMutation = useCreateVehicleMutation(this.companyId);

  protected readonly isConfirming = computed(() => 
    this.createVehicleMutation.isPending() || this.checkInMutation.isPending()
  );

  // Estados locais do relógio
  readonly timeString = signal('');
  readonly dateString = signal('');
  private clockInterval: ReturnType<typeof setInterval> | null = null;

  // Total de vagas do pátio
  readonly totalSpots = 120;

  // Formulário
  readonly plate = signal('');
  readonly modelName = signal('');
  readonly vehicleType = signal('Carro');
  readonly selectedSpot = signal(0);
  readonly isMonthly = signal(false);

  // Mapeamento das 26 cores brasileiras de veículos
  readonly VEICULOS_CORES = {
    branco: { hex: '#FFFFFF', label: 'Branco' },
    creme: { hex: '#FFFDD0', label: 'Creme' },
    prata: { hex: '#C0C0C0', label: 'Prata' },
    cinza: { hex: '#708090', label: 'Cinza' },
    grafite: { hex: '#4F5D65', label: 'Grafite' },
    chumbo: { hex: '#374151', label: 'Chumbo' },
    preto: { hex: '#1C1C1C', label: 'Preto' },
    vermelho: { hex: '#D32F2F', label: 'Vermelho' },
    vinho: { hex: '#58111A', label: 'Vinho' },
    rosa: { hex: '#FF69B4', label: 'Rosa' },
    roxo: { hex: '#4B0082', label: 'Roxo' },
    azul_claro: { hex: '#7EC8E3', label: 'Azul Claro' },
    azul_royal: { hex: '#0040FF', label: 'Azul Royal' },
    azul: { hex: '#1C3B57', label: 'Azul' },
    ciano: { hex: '#00A896', label: 'Ciano' },
    verde_claro: { hex: '#A3E635', label: 'Verde Claro' },
    verde: { hex: '#1B4D3E', label: 'Verde' },
    verde_militar: { hex: '#4B5320', label: 'Verde Militar' },
    bege: { hex: '#F5F5DC', label: 'Bege' },
    champanhe: { hex: '#EEDC82', label: 'Champanhe' },
    dourado: { hex: '#D4AF37', label: 'Dourado' },
    bronze: { hex: '#A87C43', label: 'Bronze' },
    marrom: { hex: '#5C4033', label: 'Marrom' },
    amarelo: { hex: '#F9A602', label: 'Amarelo' },
    laranja: { hex: '#E65C00', label: 'Laranja' },
    outro: { hex: '#5A5A5A', label: 'Outro' }
  };

  readonly selectedColor = signal<string>('branco');
  readonly customColor = signal<string>('');
  readonly colorSearchQuery = signal<string>('Branco');
  readonly colorDropdownOpen = signal<boolean>(false);

  // Filtra as cores com base no texto digitado no input de busca
  protected readonly filteredColors = computed(() => {
    const query = this.colorSearchQuery().toLowerCase().trim();
    const list = Object.entries(this.VEICULOS_CORES).map(([key, info]) => ({
      key,
      hex: info.hex,
      label: info.label
    }));

    if (!query) return list;

    // Filtra por aproximação (ex: "ver" para Vermelho, Verde, Vinho, Verde Claro, Verde Militar)
    return list.filter(cor => cor.label.toLowerCase().includes(query));
  });

  protected toggleColorDropdown(): void {
    this.colorDropdownOpen.update(v => !v);
  }

  protected openColorDropdown(): void {
    this.colorDropdownOpen.set(true);
    // Limpa a busca para exibir todas as cores disponíveis ao focar
    this.colorSearchQuery.set('');
  }

  protected closeColorDropdown(): void {
    // Timeout para permitir que o evento de clique no item seja disparado antes de ocultar o dropdown
    setTimeout(() => {
      this.colorDropdownOpen.set(false);
      
      // Restaura o label da cor selecionada se o usuário sair sem selecionar ou deixar em branco
      const active = this.selectedColor();
      if (!this.colorSearchQuery()) {
        const info = (this.VEICULOS_CORES as any)[active];
        this.colorSearchQuery.set(info ? info.label : 'Branco');
      }
    }, 200);
  }

  protected onColorSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.colorSearchQuery.set(input.value);
  }

  protected selectColorOption(colorKey: string): void {
    this.selectedColor.set(colorKey);
    const info = (this.VEICULOS_CORES as any)[colorKey];
    this.colorSearchQuery.set(info ? info.label : '');
    this.colorDropdownOpen.set(false);
  }

  protected obterHexDaCor(colorKey: string): string {
    const info = (this.VEICULOS_CORES as any)[colorKey];
    return info ? info.hex : '#FFFFFF';
  }

  // Vagas ocupadas atualmente
  protected readonly occupiedSpotsCount = computed(() => {
    return this.ticketsQuery.data()?.length || 0;
  });

  // Vagas livres atualmente
  protected readonly freeSpotsCount = computed(() => {
    return Math.max(0, this.totalSpots - this.occupiedSpotsCount());
  });

  // Lista de vagas disponíveis para o select (1 a 120 filtrando as ocupadas)
  protected readonly availableSpots = computed<VagaOption[]>(() => {
    const activeTickets = this.ticketsQuery.data() || [];
    const occupiedNumbers = new Set(activeTickets.map(t => this.spotAssignmentService.getSpot(t)));
    
    const options: VagaOption[] = [];
    for (let i = 1; i <= this.totalSpots; i++) {
      if (!occupiedNumbers.has(i)) {
        options.push({ numero: i, label: `Vaga ${i}` });
      }
    }
    return options;
  });

  ngOnInit(): void {
    this.updateClock();
    this.clockInterval = setInterval(() => this.updateClock(), 1000);
  }

  ngOnDestroy(): void {
    if (this.clockInterval) {
      clearInterval(this.clockInterval);
    }
  }

  private updateClock(): void {
    const now = new Date();
    this.timeString.set(
      now.toLocaleTimeString('pt-BR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      })
    );
    
    const formattedDate = now.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
    });
    // Capitalizar a primeira letra
    this.dateString.set(formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1));
  }

  protected confirmEntry(): void {
    const rawPlate = this.plate().toUpperCase().trim();
    const model = this.modelName().trim();
    const spot = this.selectedSpot();

    if (!rawPlate) {
      this.toastService.error('Placa do veículo é obrigatória.');
      return;
    }

    if (!model) {
      this.toastService.error('Modelo/Marca do veículo é obrigatório.');
      return;
    }

    if (spot <= 0) {
      this.toastService.error('Selecione uma vaga disponível.');
      return;
    }

    // Para fazer o check-in na API do backend:
    // Primeiro cadastramos o veículo na empresa usando a mutação do vehicle
    const vehicleColor = this.selectedColor() === 'outro' ? this.customColor().trim() : this.selectedColor();
    this.createVehicleMutation.mutate(
      {
        plate: rawPlate,
        model: model,
        color: vehicleColor || 'outro',
      },
      {
        onSuccess: (vehicleResponse) => {
          // Após cadastrar o veículo, fazemos o check-in (usando o id do veículo)
          this.checkInMutation.mutate(vehicleResponse.id, {
            onSuccess: (ticketResponse) => {
              this.spotAssignmentService.assignSpot(ticketResponse.id, spot);
              this.toastService.success(`Entrada do veículo ${rawPlate} registrada com sucesso na Vaga ${spot}!`);
              this.resetForm();
            },
            onError: () => {
              this.toastService.error('Erro ao registrar entrada (check-in) do veículo.');
            }
          });
        },
        onError: () => {
          // Caso falhe por já existir o veículo ou erro de cadastro, tentamos fazer o check-in diretamente se for possível.
          this.toastService.error('Erro ao cadastrar veículo. Verifique se a placa já está no sistema.');
        }
      }
    );
  }

  private resetForm(): void {
    this.plate.set('');
    this.modelName.set('');
    this.vehicleType.set('Carro');
    this.selectedSpot.set(0);
    this.isMonthly.set(false);
    this.selectedColor.set('branco');
    this.customColor.set('');
    this.colorSearchQuery.set('Branco');
  }
}
