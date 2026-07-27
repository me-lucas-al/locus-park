import { Component, input } from '@angular/core';

export type InlineNoticeVariant = 'info' | 'warning' | 'error';

@Component({
  selector: 'app-inline-notice',
  imports: [],
  templateUrl: './inline-notice.html',
  styleUrl: './inline-notice.css',
})
export class InlineNotice {
  readonly variant = input<InlineNoticeVariant>('info');
  readonly message = input.required<string>();
}
