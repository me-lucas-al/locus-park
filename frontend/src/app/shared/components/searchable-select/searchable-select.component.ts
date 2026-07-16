import {
  Component,
  input,
  output,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectOption } from '../../../core/domains/fipe/fipe.types';

@Component({
  selector: 'app-searchable-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './searchable-select.component.html',
  styleUrl: './searchable-select.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchableSelectComponent {
  readonly placeholder = input<string>('Selecione uma opção...');
  readonly options = input<SelectOption[]>([]);
  readonly disabled = input<boolean>(false);
  readonly isLoading = input<boolean>(false);
  readonly errorMessage = input<string | null>(null);

  readonly optionSelected = output<SelectOption>();

  protected readonly searchQuery = signal('');
  protected readonly isDropdownOpen = signal(false);
  protected readonly selectedLabel = signal('');

  protected readonly displayValue = computed(
    () => this.searchQuery() || this.selectedLabel()
  );

  protected readonly filteredOptions = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.options();
    return this.options().filter(option =>
      option.label.toLowerCase().includes(query)
    );
  });

  protected openDropdown(): void {
    if (this.disabled() || this.isLoading()) return;
    this.isDropdownOpen.set(true);
    this.searchQuery.set('');
  }

  protected closeDropdown(): void {
    setTimeout(() => {
      this.isDropdownOpen.set(false);
      this.searchQuery.set(this.selectedLabel());
    }, 200);
  }

  protected onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery.set(input.value);
  }

  protected selectOption(option: SelectOption): void {
    this.selectedLabel.set(option.label);
    this.searchQuery.set(option.label);
    this.isDropdownOpen.set(false);
    this.optionSelected.emit(option);
  }

  reset(): void {
    this.selectedLabel.set('');
    this.searchQuery.set('');
    this.isDropdownOpen.set(false);
  }


}
