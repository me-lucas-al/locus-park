import { Component, inject, signal, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ColorSelect } from '../color-select/color-select.component';
import { useCheckInMutation } from '../../../../core/domains/ticket/ticket.hooks';
import { useCreateVehicleMutation } from '../../../../core/domains/vehicle/vehicle.hooks';
import { useUserProfileQuery } from '../../../../core/domains/user/user.hooks';
import { ToastService } from '../../../../shared/services/toast.service';
import { SpotAssignmentService } from '../../../../shared/services/spot-assignment.service';

@Component({
  selector: 'app-parking-spot-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ColorSelect],
  templateUrl: './parking-spot-form.component.html',
  styleUrl: './parking-spot-form.component.css',
})
export class ParkingSpotForm {
  private readonly toastService = inject(ToastService);
  private readonly spotAssignmentService = inject(SpotAssignmentService);
  private readonly profileQuery = useUserProfileQuery();

  readonly spotNumber = input<number>(0);
  readonly confirmed = output<void>();
  readonly cancel = output<void>();

  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || '');

  protected readonly checkInMutation = useCheckInMutation();
  protected readonly createVehicleMutation = useCreateVehicleMutation(this.companyId);

  protected readonly isPending = computed(
    () => this.createVehicleMutation.isPending() || this.checkInMutation.isPending(),
  );

  protected readonly plate = signal('');
  protected readonly modelName = signal('');
  protected readonly vehicleType = signal<'CAR' | 'MOTORCYCLE' | 'VAN' | 'TRUCK'>('CAR');
  protected readonly selectedColor = signal('branco');
  protected readonly customColorName = signal('');

  protected onColorSelect(color: string): void {
    this.selectedColor.set(color);
  }

  protected submitForm(): void {
    const rawPlate = this.plate().toUpperCase().trim();
    const model = this.modelName().trim();
    const color =
      this.selectedColor() === 'outro' ? this.customColorName().trim() : this.selectedColor();

    if (!rawPlate) {
      this.toastService.error('Placa do veículo é obrigatória.');
      return;
    }
    if (!model) {
      this.toastService.error('Modelo/Marca do veículo é obrigatório.');
      return;
    }
    if (this.selectedColor() === 'outro' && !color) {
      this.toastService.error('Digite a cor personalizada.');
      return;
    }

    this.createVehicleMutation.mutate(
      { plate: rawPlate, model, color, type: this.vehicleType() },
      {
        onSuccess: (vehicleResponse) => {
          this.checkInMutation.mutate(vehicleResponse.id, {
            onSuccess: (ticketResponse) => {
              this.spotAssignmentService.assignSpot(ticketResponse.id, this.spotNumber());
              this.toastService.success(
                `Entrada registrada com sucesso na vaga ${this.spotNumber()}!`,
              );
              this.confirmed.emit();
            },
            onError: () => this.toastService.error('Erro ao realizar check-in do veículo.'),
          });
        },
        onError: () => this.toastService.error('Erro ao cadastrar veículo.'),
      },
    );
  }
}
