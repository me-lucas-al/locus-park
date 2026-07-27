import { Component, computed, input, signal } from '@angular/core';
import { ReportCell, ReportCellFormat } from '../report-cell/report-cell.component';
import { EmptyState } from '../../../../shared/components/empty-state/empty-state';

export interface ReportColumn {
  readonly key: string;
  readonly label: string;
  readonly format: ReportCellFormat;
  readonly align?: 'left' | 'right';
}

@Component({
  selector: 'app-report-table',
  imports: [ReportCell, EmptyState],
  templateUrl: './report-table.component.html',
  styleUrl: './report-table.component.css',
})
export class ReportTable {
  readonly columns = input.required<readonly ReportColumn[]>();
  readonly rows = input.required<readonly object[]>();
  readonly pageSize = input(50);

  protected readonly page = signal(0);

  protected readonly totalPages = computed(() => Math.max(1, Math.ceil(this.rows().length / this.pageSize())));

  protected readonly pageRows = computed(() => {
    const start = this.page() * this.pageSize();
    return this.rows().slice(start, start + this.pageSize());
  });

  protected readonly rangeLabel = computed(() => {
    const total = this.rows().length;
    if (total === 0) return '0–0 de 0';
    const start = this.page() * this.pageSize() + 1;
    const end = Math.min(total, start + this.pageSize() - 1);
    return `${start}–${end} de ${total}`;
  });

  protected previous(): void {
    this.page.update((p) => Math.max(0, p - 1));
  }

  protected next(): void {
    this.page.update((p) => Math.min(this.totalPages() - 1, p + 1));
  }

  protected cellValue(row: object, key: string): unknown {
    return (row as Record<string, unknown>)[key];
  }
}
