import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { useTicketsQuery } from '../../core/domains/ticket/ticket.hooks';
import { useReportQuery } from '../../core/domains/report/report.hooks';
import { useUserProfileQuery } from '../../core/domains/user/user.hooks';
import { TicketResponse } from '../../core/domains/ticket/ticket.types';
import { ModalExit } from '../../shared/components/modal-exit/modal-exit.component';
import { SpotAssignmentService } from '../../shared/services/spot-assignment.service';

interface GridSpot {
  number: number;
  ticket: TicketResponse | null;
  status: 'Livre' | 'Ocupada';
}

interface LayoutItem {
  type: 'spot' | 'corridor' | 'empty';
  spot?: GridSpot;
  corridorIndex?: number;
  direction?: 'left' | 'right';
  spotRowIndex?: number;
  orientation?: 'top-open' | 'bottom-open';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ModalExit],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly router = inject(Router);
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  // Queries
  protected readonly ticketsQuery = useTicketsQuery();
  protected readonly profileQuery = useUserProfileQuery();

  // Signals
  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || null);
  protected readonly reportQuery = useReportQuery(this.companyId);

  // Configuração de Vagas Totais (reativo)
  readonly totalSpots = signal<number>(120);

  // Modal checkout
  readonly modalSaidaAberto = signal(false);
  readonly veiculoSelecionado = signal<TicketResponse | null>(null);

  // Vagas ocupadas atualmente
  protected readonly occupiedSpotsCount = computed(() => {
    return this.ticketsQuery.data()?.length || 0;
  });

  // Vagas livres atualmente
  protected readonly freeSpotsCount = computed(() => {
    return Math.max(0, this.totalSpots() - this.occupiedSpotsCount());
  });

  // Número de vagas por fileira, calculado dinamicamente com base no total de vagas
  protected readonly spotsPerRow = computed<number>(() => {
    const total = this.totalSpots();
    if (total <= 15) return 5;
    if (total <= 40) return 8;
    if (total <= 80) return 10;
    return 12;
  });

  // Mapeamento dinâmico das vagas de 1 a totalSpots para o mapa visual
  protected readonly gridSpots = computed<GridSpot[]>(() => {
    const activeTickets = this.ticketsQuery.data() || [];
    this.spotAssignmentService.cleanInactiveTickets(activeTickets);

    const ticketMap = new Map<number, TicketResponse>();
    activeTickets.forEach((t) => {
      const spot = this.spotAssignmentService.getSpot(t);
      if (spot) {
        ticketMap.set(spot, t);
      }
    });

    const spots: GridSpot[] = [];
    const total = this.totalSpots();
    for (let i = 1; i <= total; i++) {
      const ticket = ticketMap.get(i) || null;
      spots.push({
        number: i,
        ticket,
        status: ticket ? 'Ocupada' : 'Livre',
      });
    }
    return spots;
  });

  // Organiza as vagas e insere corredores de circulação e asfalto de forma reativa
  protected readonly layoutItems = computed<LayoutItem[]>(() => {
    const spots = this.gridSpots();
    const spotsPerRow = this.spotsPerRow();
    const items: LayoutItem[] = [];

    // Adiciona o primeiro corredor
    items.push({
      type: 'corridor',
      direction: 'right',
      corridorIndex: 0,
    });

    let i = 0;
    let fileiraCountInSet = 0;
    let spotRowIndex = 0;

    while (i < spots.length) {
      const rowSpots = spots.slice(i, i + spotsPerRow);
      i += spotsPerRow;
      spotRowIndex++;

      const orientation = spotRowIndex % 2 !== 0 ? 'top-open' : 'bottom-open';

      rowSpots.forEach((spot) => {
        items.push({
          type: 'spot',
          spot,
          spotRowIndex,
          orientation,
        });
      });

      if (rowSpots.length < spotsPerRow) {
        const emptyCount = spotsPerRow - rowSpots.length;
        for (let e = 0; e < emptyCount; e++) {
          items.push({
            type: 'empty',
          });
        }
      }

      fileiraCountInSet++;

      if (fileiraCountInSet === 2 && i < spots.length) {
        const corridorsCount = items.filter((item) => item.type === 'corridor').length;
        items.push({
          type: 'corridor',
          direction: corridorsCount % 2 === 0 ? 'right' : 'left',
          corridorIndex: corridorsCount,
        });
        fileiraCountInSet = 0;
      }
    }

    if (items.length > 0 && items[items.length - 1].type !== 'corridor') {
      const corridorsCount = items.filter((item) => item.type === 'corridor').length;
      items.push({
        type: 'corridor',
        direction: corridorsCount % 2 === 0 ? 'right' : 'left',
        corridorIndex: corridorsCount,
      });
    }

    return items;
  });

  // Atualiza a quantidade total de vagas
  protected updateTotalSpots(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (!isNaN(value) && value > 0) {
      this.totalSpots.set(value);
    }
  }

  // Detalhes das vagas ocupadas (apenas os tickets ativos)
  protected readonly occupiedTickets = computed(() => {
    return this.ticketsQuery.data() || [];
  });

  protected getSpotNumber(ticket: TicketResponse): number {
    return this.spotAssignmentService.getSpot(ticket);
  }

  protected obterCorHex(ticket: TicketResponse | null): string {
    if (!ticket || !ticket.vehicle) return '#FFFFFF';
    const cor = ticket.vehicle.color?.toLowerCase().trim();
    if (!cor) return '#FFFFFF';
    
    // Se for hexadecimal direto
    if (cor.startsWith('#')) return cor;
    
    // Mapeamento de cores automotivas brasileiras
    const coresMap: Record<string, string> = {
      branco: '#FFFFFF',
      creme: '#FFFDD0',
      prata: '#C0C0C0',
      cinza: '#708090',
      grafite: '#4F5D65',
      chumbo: '#374151',
      preto: '#1C1C1C',
      vermelho: '#D32F2F',
      vinho: '#58111A',
      rosa: '#FF69B4',
      roxo: '#4B0082',
      azul_claro: '#7EC8E3',
      azul_royal: '#0040FF',
      azul: '#1C3B57',
      ciano: '#00A896',
      verde_claro: '#A3E635',
      verde: '#1B4D3E',
      verde_militar: '#4B5320',
      bege: '#F5F5DC',
      champanhe: '#EEDC82',
      dourado: '#D4AF37',
      bronze: '#A87C43',
      marrom: '#5C4033',
      amarelo: '#F9A602',
      laranja: '#E65C00',
      outro: '#5A5A5A'
    };
    
    if (coresMap[cor]) {
      return coresMap[cor];
    }
    
    // Se for 'n/a' ou não listado, geramos um hexadecimal determinístico bonito baseado na placa
    const plate = ticket.vehicle.plate || '';
    if (plate && plate !== 'N/A') {
      const coresDisponiveis = [
        '#FFFFFF', '#C0C0C0', '#708090', '#1C1C1C', '#D32F2F', 
        '#1C3B57', '#1B4D3E', '#F5F5DC', '#A87C43', '#5C4033'
      ];
      let hash = 0;
      for (let i = 0; i < plate.length; i++) {
        hash = plate.charCodeAt(i) + ((hash << 5) - hash);
      }
      const index = Math.abs(hash) % coresDisponiveis.length;
      return coresDisponiveis[index];
    }
    
    return '#5A5A5A'; // Neutro padrão para outros casos
  }

  protected formatarDataAtual(): string {
    const data = new Date();
    return data.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  }

  protected irParaEntrada(): void {
    this.router.navigate(['/entry']);
  }

  protected abrirModalSaida(ticket: TicketResponse): void {
    this.veiculoSelecionado.set(ticket);
    this.modalSaidaAberto.set(true);
  }

  protected fecharModalSaida(): void {
    this.modalSaidaAberto.set(false);
    this.veiculoSelecionado.set(null);
  }

  protected handleCheckoutConfirmed(): void {
    this.ticketsQuery.refetch();
    if (this.companyId() && this.companyId() !== 'null' && this.companyId() !== 'undefined') {
      this.reportQuery.refetch();
    }
  }
}