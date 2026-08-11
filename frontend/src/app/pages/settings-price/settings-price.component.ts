import { Component, inject, signal, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  useTariffQuery,
  useUpdateTariffMutation,
  usePricingQuery,
  useUpdatePricingMutation,
  useVehiclePricingQuery,
  useUpdateVehiclePricingMutation,
} from '../../core/domains/tariff/tariff.hooks';
import {
  usePartnershipsQuery,
  useCreatePartnershipMutation,
  useUpdatePartnershipMutation,
  useDeletePartnershipMutation,
} from '../../core/domains/partnership/partnership.hooks';
import { ToastService } from '../../shared/services/toast.service';
import { getBackendErrorMessage } from '../../core/utils/error-handler.util';
import { LoadingDirective } from '../../shared/directives/loading.directive';

@Component({
  selector: 'app-settings-price',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective],
  templateUrl: './settings-price.html',
  styleUrl: './settings-price.css',
})
export class SettingsPrice {
  private readonly toastService = inject(ToastService);

  // Queries e Mutações
  protected readonly tariffQuery = useTariffQuery();
  protected readonly pricingQuery = usePricingQuery();
  protected readonly vehiclePricingQuery = useVehiclePricingQuery();
  protected readonly partnershipsQuery = usePartnershipsQuery();

  protected readonly updateTariffMutation = useUpdateTariffMutation();
  protected readonly updatePricingMutation = useUpdatePricingMutation();
  protected readonly updateVehiclePricingMutation = useUpdateVehiclePricingMutation();
  protected readonly createPartnershipMutation = useCreatePartnershipMutation();
  protected readonly updatePartnershipMutation = useUpdatePartnershipMutation();
  protected readonly deletePartnershipMutation = useDeletePartnershipMutation();

  protected readonly isSavingTariffs = computed(() => 
    this.updateTariffMutation.isPending() || this.updatePricingMutation.isPending()
  );

  protected readonly isSavingVehiclePricing = computed(() =>
    this.updateVehiclePricingMutation.isPending()
  );

  protected readonly isAddingPartnership = computed(() => 
    this.createPartnershipMutation.isPending() || this.updatePartnershipMutation.isPending()
  );

  // Controle de Abas
  readonly activeTab = signal<'tariffs' | 'daily' | 'vehicle-types' | 'partnerships' | 'rules'>('tariffs');

  // Formulário Tarifas
  readonly firstHourRate = signal(10.00);
  readonly additionalHourRate = signal(8.00);
  readonly timeFractioningMinutes = signal(60);
  readonly gracePeriodMinutes = signal(10);

  // Formulário Diária & Mensalistas
  readonly dailyTriggerHours = signal(12);
  readonly dailyValue = signal(40.00);
  readonly monthlyMemberFee = signal(250.00);
  readonly overnightStayFee = signal(20.00);

  // Formulário Multiplicadores por Tipo de Veículo
  readonly motorcycleMultiplier = signal(0.60);
  readonly carMultiplier = signal(1.00);
  readonly vanMultiplier = signal(1.30);
  readonly truckMultiplier = signal(1.50);

  // Exemplos de preço calculados dinamicamente
  readonly motorcycleExampleFirstHour = computed(() =>
    (this.firstHourRate() * this.motorcycleMultiplier()).toFixed(2)
  );
  readonly carExampleFirstHour = computed(() =>
    (this.firstHourRate() * this.carMultiplier()).toFixed(2)
  );
  readonly vanExampleFirstHour = computed(() =>
    (this.firstHourRate() * this.vanMultiplier()).toFixed(2)
  );
  readonly truckExampleFirstHour = computed(() =>
    (this.firstHourRate() * this.truckMultiplier()).toFixed(2)
  );

  // Formulário Convênios
  readonly newPartnershipName = signal('');
  readonly newPartnershipDiscountType = signal('PERCENTAGE');
  readonly newPartnershipValue = signal(10);

  // Controle de Edição de Convênios
  readonly editingPartnershipId = signal<string | null>(null);
  readonly editingPartnershipName = signal('');
  readonly editingPartnershipDiscountType = signal('PERCENTAGE');
  readonly editingPartnershipValue = signal(0);

  // Controle de Regras Adicionais
  readonly lostTicketRate = signal(30.00);
  readonly overnightStartHours = signal(localStorage.getItem('overnightStartHours') || '22:00');

  protected readonly isSavingExtra = computed(() => this.updateTariffMutation.isPending());

  constructor() {
    // Efeito para carregar dados das queries nos inputs do formulário de tarifas
    effect(() => {
      const tariff = this.tariffQuery.data();
      if (tariff) {
        this.firstHourRate.set(tariff.firstHourValue);
        this.additionalHourRate.set(tariff.additionalFractionValue);
        this.gracePeriodMinutes.set(tariff.toleranceMinutes);
        this.overnightStayFee.set(tariff.overnightFee);
        if (tariff.lostTicketFee !== undefined && tariff.lostTicketFee !== null) {
          this.lostTicketRate.set(tariff.lostTicketFee);
        }
      }
    });

    // Efeito para carregar dados de pricing
    effect(() => {
      const pricing = this.pricingQuery.data();
      if (pricing) {
        this.dailyTriggerHours.set(pricing.dailyTriggerHours);
        this.dailyValue.set(pricing.dailyValue);
        this.monthlyMemberFee.set(pricing.monthlyBaseValue);
      }
    });

    // Efeito para carregar dados de multiplicadores por veículo
    effect(() => {
      const vehiclePricings = this.vehiclePricingQuery.data();
      if (vehiclePricings && vehiclePricings.length > 0) {
        for (const vp of vehiclePricings) {
          if (vp.vehicleType === 'MOTORCYCLE') {
            this.motorcycleMultiplier.set(vp.multiplier);
          } else if (vp.vehicleType === 'CAR') {
            this.carMultiplier.set(vp.multiplier);
          } else if (vp.vehicleType === 'VAN') {
            this.vanMultiplier.set(vp.multiplier);
          } else if (vp.vehicleType === 'TRUCK') {
            this.truckMultiplier.set(vp.multiplier);
          }
        }
      }
    });
  }

  protected changeTab(tab: 'tariffs' | 'daily' | 'vehicle-types' | 'partnerships' | 'rules'): void {
    this.activeTab.set(tab);
  }

  protected saveTariffs(): void {
    this.updateTariffMutation.mutate(
      {
        firstHourValue: this.firstHourRate(),
        additionalFractionValue: this.additionalHourRate(),
        toleranceMinutes: this.gracePeriodMinutes(),
        overnightFee: this.overnightStayFee(),
        lostTicketFee: this.lostTicketRate(),
      },
      {
        onSuccess: () => {
          this.updatePricingMutation.mutate(
            {
              dailyTriggerHours: this.dailyTriggerHours(),
              dailyValue: this.dailyValue(),
              monthlyBaseValue: this.monthlyMemberFee(),
            },
            {
              onSuccess: () => {
                this.toastService.success('Configurações salvas com sucesso!');
              },
              onError: () => {
                this.toastService.error('Erro ao atualizar configurações tarifárias adicionais.');
              }
            }
          );
        },
        onError: () => {
          this.toastService.error('Erro ao atualizar configurações tarifárias principais.');
        }
      }
    );
  }

  protected saveVehiclePricing(): void {
    this.updateVehiclePricingMutation.mutate(
      {
        multipliers: [
          { vehicleType: 'MOTORCYCLE', multiplier: this.motorcycleMultiplier() },
          { vehicleType: 'CAR', multiplier: this.carMultiplier() },
          { vehicleType: 'VAN', multiplier: this.vanMultiplier() },
          { vehicleType: 'TRUCK', multiplier: this.truckMultiplier() },
        ],
      },
      {
        onSuccess: () => {
          this.toastService.success('Preços por tipo de veículo salvos com sucesso!');
        },
        onError: (err: any) => {
          const errMsg = getBackendErrorMessage(err, 'Erro ao atualizar preços por tipo de veículo.');
          this.toastService.error(errMsg);
        }
      }
    );
  }

  protected addPartnership(): void {
    const name = this.newPartnershipName().trim();
    const type = this.newPartnershipDiscountType();
    const val = this.newPartnershipValue();

    if (!name) {
      this.toastService.error('O nome do convênio/parceria é obrigatório.');
      return;
    }

    if (val <= 0) {
      this.toastService.error('O valor do desconto deve ser maior do que zero.');
      return;
    }

    this.createPartnershipMutation.mutate(
      {
        name,
        discountType: type,
        value: val,
      },
      {
        onSuccess: () => {
          this.toastService.success('Parceria cadastrada com sucesso!');
          this.newPartnershipName.set('');
          this.newPartnershipValue.set(10);
        },
        onError: () => {
          this.toastService.error('Erro ao cadastrar nova parceria.');
        }
      }
    );
  }

  protected deletePartnership(id: string): void {
    if (confirm('Tem certeza que deseja excluir esta parceria/convênio?')) {
      this.deletePartnershipMutation.mutate(id, {
        onSuccess: () => {
          this.toastService.success('Parceria excluída com sucesso!');
        },
        onError: () => {
          this.toastService.error('Erro ao excluir parceria.');
        }
      });
    }
  }

  protected startEdit(partnership: any): void {
    this.editingPartnershipId.set(partnership.id);
    this.editingPartnershipName.set(partnership.name);
    this.editingPartnershipDiscountType.set(partnership.discountType.toUpperCase());
    this.editingPartnershipValue.set(partnership.value);
  }

  protected cancelEdit(): void {
    this.editingPartnershipId.set(null);
  }

  protected saveEdit(partnershipId: string): void {
    const name = this.editingPartnershipName().trim();
    const type = this.editingPartnershipDiscountType();
    const val = this.editingPartnershipValue();

    if (!name) {
      this.toastService.error('O nome do convênio/parceria é obrigatório.');
      return;
    }

    if (val <= 0) {
      this.toastService.error('O valor do desconto deve ser maior do que zero.');
      return;
    }

    this.updatePartnershipMutation.mutate(
      {
        id: partnershipId,
        request: {
          name,
          discountType: type,
          value: val,
        },
      },
      {
        onSuccess: () => {
          this.toastService.success('Parceria atualizada com sucesso!');
          this.editingPartnershipId.set(null);
        },
        onError: (err: any) => {
          const errMsg = getBackendErrorMessage(err, 'Erro ao atualizar parceria.');
          this.toastService.error(errMsg);
        },
      }
    );
  }

  protected saveExtraSettings(): void {
    this.updateTariffMutation.mutate(
      {
        firstHourValue: this.firstHourRate(),
        additionalFractionValue: this.additionalHourRate(),
        toleranceMinutes: this.gracePeriodMinutes(),
        overnightFee: this.overnightStayFee(),
        lostTicketFee: this.lostTicketRate(),
      },
      {
        onSuccess: () => {
          localStorage.setItem('overnightStartHours', this.overnightStartHours());
          this.toastService.success('Configurações adicionais salvas com sucesso!');
        },
        onError: () => {
          this.toastService.error('Erro ao atualizar configurações adicionais.');
        }
      }
    );
  }
}
