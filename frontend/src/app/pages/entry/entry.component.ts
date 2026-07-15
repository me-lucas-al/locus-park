import { Component, OnInit, OnDestroy, inject, signal, computed, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { useTicketsQuery, useCheckInMutation } from '../../core/domains/ticket/ticket.hooks';
import { useCreateVehicleMutation } from '../../core/domains/vehicle/vehicle.hooks';
import { useUserProfileQuery } from '../../core/domains/user/user.hooks';
import { ToastService } from '../../shared/services/toast.service';
import { SpotAssignmentService } from '../../shared/services/spot-assignment.service';
import { LoadingDirective } from '../../shared/directives/loading.directive';
import { ColorSelectComponent } from '../../shared/components/color-select/color-select.component';
import { SpotOption } from './entry.types';

@Component({
  selector: 'app-entry',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective, ColorSelectComponent],
  templateUrl: './entry.component.html',
  styleUrl: './entry.component.css',
})
export class Entry implements OnInit, OnDestroy {
  private readonly toastService = inject(ToastService);
  private readonly spotAssignmentService = inject(SpotAssignmentService);
  @ViewChild(ColorSelectComponent) colorSelect?: ColorSelectComponent;

  protected readonly ticketsQuery = useTicketsQuery();
  protected readonly profileQuery = useUserProfileQuery();
  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || '');
  protected readonly checkInMutation = useCheckInMutation();
  protected readonly createVehicleMutation = useCreateVehicleMutation(this.companyId);
  protected readonly isConfirming = computed(
    () => this.createVehicleMutation.isPending() || this.checkInMutation.isPending(),
  );

  readonly timeString = signal('');
  readonly dateString = signal('');
  private clockInterval: ReturnType<typeof setInterval> | null = null;
  readonly totalSpots = 120;

  readonly plate = signal('');
  readonly modelName = signal('');
  readonly vehicleType = signal('Carro');
  readonly selectedSpot = signal(0);
  readonly isMonthly = signal(false);
  readonly vehicleColor = signal('branco');
  readonly vehicleCustomColor = signal('');

  protected readonly occupiedSpotsCount = computed(() => this.ticketsQuery.data()?.length || 0);
  protected readonly freeSpotsCount = computed(() =>
    Math.max(0, this.totalSpots - this.occupiedSpotsCount()),
  );

  protected readonly availableSpots = computed<SpotOption[]>(() => {
    const activeTickets = this.ticketsQuery.data() || [];
    const occupiedNumbers = new Set(
      activeTickets.map((t) => this.spotAssignmentService.getSpot(t)),
    );
    const options: SpotOption[] = [];
    for (let i = 1; i <= this.totalSpots; i++) {
      if (!occupiedNumbers.has(i)) {
        options.push({ number: i, label: `Vaga ${i}` });
      }
    }
    return options;
  });

  ngOnInit(): void {
    this.updateClock();
    this.clockInterval = setInterval(() => this.updateClock(), 1000);
  }

  ngOnDestroy(): void {
    if (this.clockInterval) clearInterval(this.clockInterval);
  }

  private updateClock(): void {
    const now = new Date();
    this.timeString.set(
      now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    );
    const date = now.toLocaleDateString('pt-BR', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
    });
    this.dateString.set(date.charAt(0).toUpperCase() + date.slice(1));
  }

  protected confirmEntry(): void {
    const rawPlate = this.plate().toUpperCase().trim();
    const model = this.modelName().trim();
    const spot = this.selectedSpot();

    if (!rawPlate) return this.toastService.error('Placa do veículo é obrigatória.');
    if (!model) return this.toastService.error('Modelo/Marca do veículo é obrigatório.');
    if (spot <= 0) return this.toastService.error('Selecione uma vaga disponível.');

    const colorValue =
      this.vehicleColor() === 'outro' ? this.vehicleCustomColor().trim() : this.vehicleColor();
    this.createVehicleMutation.mutate(
      { plate: rawPlate, model, color: colorValue || 'outro' },
      {
        onSuccess: (res) => {
          this.checkInMutation.mutate(res.id, {
            onSuccess: (ticket) => {
              this.spotAssignmentService.assignSpot(ticket.id, spot);
              this.toastService.success(`Entrada registrada na Vaga ${spot}!`);
              this.resetForm();
            },
            onError: () => this.toastService.error('Erro ao registrar entrada do veículo.'),
          });
        },
        onError: () =>
          this.toastService.error(
            'Erro ao cadastrar veículo. Verifique se a placa já está no sistema.',
          ),
      },
    );
  }

  private resetForm(): void {
    this.plate.set('');
    this.modelName.set('');
    this.vehicleType.set('Carro');
    this.selectedSpot.set(0);
    this.isMonthly.set(false);
    this.colorSelect?.reset();
  }
}
