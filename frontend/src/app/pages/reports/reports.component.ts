import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { useReportQuery } from '../../core/domains/report/report.hooks';
import { buildRange, DEFAULT_RANGE_PRESET } from '../../core/utils/date-range.factory';
import { ToastService } from '../../shared/services/toast.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css',
})
export class Reports {
  private readonly toastService = inject(ToastService);

  // Signals
  protected readonly range = signal(buildRange(DEFAULT_RANGE_PRESET, new Date()));
  protected readonly reportQuery = useReportQuery(this.range);

  // Formatação do tempo médio
  protected readonly averageStayFormatted = computed(() => {
    const minutes = this.reportQuery.data()?.averageStayMinutes || 0;
    const h = Math.floor(minutes / 60);
    const m = Math.round(minutes % 60);
    return `${h}h ${m}m`;
  });

  protected exportCSV(): void {
    const data = this.reportQuery.data();
    if (!data || data.paymentMethodSummaries.length === 0) {
      this.toastService.error('Não há dados disponíveis para exportação.');
      return;
    }

    // Criar conteúdo CSV
    let csvContent = 'data:text/csv;charset=utf-8,';
    csvContent += 'Metodo de Pagamento,Faturamento (R$),Atendimentos\n';

    data.paymentMethodSummaries.forEach((summary) => {
      csvContent += `${summary.method},${summary.revenue.toFixed(2)},${summary.ticketCount}\n`;
    });

    // Adicionar totais
    csvContent += `Total Geral,${data.totalRevenue.toFixed(2)},${data.totalServices}\n`;

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `relatorio_faturamento_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.toastService.success('CSV exportado com sucesso!');
  }

  protected formatPaymentMethod(method: string): string {
    switch (method) {
      case 'DINHEIRO': return 'Dinheiro';
      case 'PIX': return 'PIX';
      case 'CARD_CREDIT': return 'Cartão de Crédito';
      case 'CARD_DEBIT': return 'Cartão de Débito';
      default: return method;
    }
  }
}
