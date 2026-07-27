import { Component, EventEmitter, Input, OnChanges, Output, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TicketResponse, PaymentMethod } from '../../../core/domains/ticket/ticket.types';
import { useCheckOutMutation, useApplyPartnershipMutation } from '../../../core/domains/ticket/ticket.hooks';
import { usePartnershipsQuery } from '../../../core/domains/partnership/partnership.hooks';
import { ToastService } from '../../services/toast.service';
import { SpotAssignmentService } from '../../services/spot-assignment.service';
import { paymentMethodLabel } from '../../utils/payment-method-label';
import { LoadingDirective } from '../../directives/loading.directive';

@Component({
  selector: 'app-modal-exit',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective],
  templateUrl: './modal-exit.html',
  styleUrl: './modal-exit.css',
})
export class ModalExit implements OnChanges {
  @Input() ticket: TicketResponse | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<void>();

  private readonly toastService = inject(ToastService);
  private readonly spotAssignmentService = inject(SpotAssignmentService);

  // Mutações e Queries
  protected readonly checkOutMutation = useCheckOutMutation();
  protected readonly applyPartnershipMutation = useApplyPartnershipMutation();
  protected readonly partnershipsQuery = usePartnershipsQuery();

  protected readonly isCheckingOut = computed(() => 
    this.checkOutMutation.isPending() || this.applyPartnershipMutation.isPending()
  );

  protected getSpotNumber(): number {
    return this.ticket ? this.spotAssignmentService.getSpot(this.ticket) : 0;
  }

  // Estados locais
  readonly currentDateTime = signal(new Date());
  readonly elapsedTime = signal('0h 0m');
  readonly selectedPartnershipId = signal('');
  readonly selectedPaymentMethod = signal<PaymentMethod | ''>('');

  // Valores de cálculo
  readonly baseRate = signal(0);
  readonly discountValue = signal(0);

  readonly finalRate = computed(() => {
    const base = this.baseRate();
    const discount = this.discountValue();
    return Math.max(0, base - discount);
  });

  protected readonly paymentMethodLabel = paymentMethodLabel;
  readonly paymentMethods: readonly PaymentMethod[] = ['DINHEIRO', 'PIX', 'CARD_CREDIT', 'CARD_DEBIT'];

  ngOnChanges(): void {
    if (this.ticket) {
      this.currentDateTime.set(new Date());
      this.selectedPartnershipId.set('');
      this.selectedPaymentMethod.set('');
      this.calculateValues();
    }
  }

  private calculateValues(): void {
    if (!this.ticket) return;

    const entered = new Date(this.ticket.enteredAt);
    const diffMs = this.currentDateTime().getTime() - entered.getTime();
    const diffMin = Math.max(1, Math.floor(diffMs / 60000));
    
    const h = Math.floor(diffMin / 60);
    const m = diffMin % 60;
    this.elapsedTime.set(`${h}h ${m}m`);

    // Regra de preço simulada simples (R$ 10,00 na 1ª hora, R$ 8,00 nas adicionais)
    const hours = Math.ceil(diffMin / 60);
    let calculatedBase = 0;
    if (hours <= 1) {
      calculatedBase = 10.00;
    } else {
      calculatedBase = 10.00 + (hours - 1) * 8.00;
    }

    // Se passar de 8h, cobra diária de R$ 60,00
    if (hours >= 8) {
      calculatedBase = 60.00 * Math.ceil(hours / 24);
    }

    this.baseRate.set(calculatedBase);
    this.discountValue.set(0);
  }

  protected handlePartnershipChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const partnershipId = select.value;
    this.selectedPartnershipId.set(partnershipId);

    if (!partnershipId) {
      this.discountValue.set(0);
      return;
    }

    const partnerships = this.partnershipsQuery.data() || [];
    const selected = partnerships.find((p) => p.id === partnershipId);
    
    if (selected) {
      if (selected.discountType && selected.discountType.toUpperCase() === 'PERCENTAGE') {
        const disc = (this.baseRate() * selected.value) / 100;
        this.discountValue.set(disc);
      } else {
        this.discountValue.set(selected.value);
      }
    }
  }

  protected confirmCheckout(): void {
    const payment = this.selectedPaymentMethod();
    const ticketId = this.ticket?.id;

    if (!payment) {
      this.toastService.error('Selecione a forma de pagamento.');
      return;
    }

    if (!ticketId) return;

    // Se houver convênio selecionado, aplica no ticket primeiro
    const partnershipId = this.selectedPartnershipId();
    if (partnershipId) {
      this.applyPartnershipMutation.mutate(
        { id: ticketId, partnershipId },
        {
          onSuccess: () => {
            this.executeCheckout(ticketId, payment);
          },
          onError: () => {
            this.toastService.error('Erro ao aplicar o convênio/parceria no ticket.');
          }
        }
      );
    } else {
      this.executeCheckout(ticketId, payment);
    }
  }

  private executeCheckout(id: string, paymentMethod: PaymentMethod): void {
    this.checkOutMutation.mutate(
      { id, paymentMethod },
      {
        onSuccess: () => {
          this.spotAssignmentService.releaseSpot(id);
          this.toastService.success(`Veículo liberado com sucesso! Vaga desocupada.`);
          this.confirmed.emit();
          this.close.emit();
        },
        onError: () => {
          this.toastService.error('Erro ao registrar a saída do veículo.');
        }
      }
    );
  }
}
