import { Component, input } from '@angular/core';

export type SummaryCardTone = 'slate' | 'green' | 'red' | 'blue' | 'purple' | 'amber';

@Component({
  selector: 'app-summary-card',
  imports: [],
  templateUrl: './summary-card.html',
  styleUrl: './summary-card.css',
})
export class SummaryCard {
  readonly label = input.required<string>();
  readonly value = input.required<string | null>();
  readonly hint = input('');
  readonly tone = input<SummaryCardTone>('slate');
}
