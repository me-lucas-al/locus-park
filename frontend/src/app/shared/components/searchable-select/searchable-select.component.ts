import {
  Component,
  input,
  output,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectOption } from '../../../core/domains/vehicle-catalog/vehicle-catalog.types';

const OTHER_OPTION_CODE = 'outro';

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
  protected readonly selectedOption = signal<SelectOption | null>(null);
  protected readonly customOtherText = signal('');

  protected readonly isOtherOptionSelected = computed(
    () => this.selectedOption()?.code === OTHER_OPTION_CODE
  );

  protected readonly filteredOptions = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const selected = this.selectedOption();
    if (!query || (selected && selected.label.toLowerCase() === query)) {
      return this.options();
    }
    return this.options().filter(option =>
      option.label.toLowerCase().includes(query)
    );
  });

  protected openDropdown(): void {
    if (this.disabled() || this.isLoading()) return;
    this.isDropdownOpen.set(true);
  }

  protected closeDropdown(): void {
    setTimeout(() => {
      this.isDropdownOpen.set(false);
      const selected = this.selectedOption();
      if (selected) {
        if (selected.code === OTHER_OPTION_CODE) {
          this.searchQuery.set(this.customOtherText() || selected.label);
        } else {
          this.searchQuery.set(selected.label);
        }
      } else {
        this.searchQuery.set('');
      }
    }, 200);
  }

  protected onSearchInput(event: Event): void {
    const inputEl = event.target as HTMLInputElement;
    const value = inputEl.value;
    this.searchQuery.set(value);
    this.isDropdownOpen.set(true);

    if (!value.trim()) {
      this.selectedOption.set(null);
      this.customOtherText.set('');
      this.optionSelected.emit({ code: '', label: '' });
    }
  }

  protected selectOption(option: SelectOption): void {
    this.selectedOption.set(option);
    this.customOtherText.set('');
    this.searchQuery.set(option.label);
    this.isDropdownOpen.set(false);
    this.optionSelected.emit(option);
  }

  protected onCustomOtherInput(event: Event): void {
    const typedValue = (event.target as HTMLInputElement).value;
    this.customOtherText.set(typedValue);
    this.optionSelected.emit({ code: OTHER_OPTION_CODE, label: typedValue });
  }

  reset(): void {
    this.selectedOption.set(null);
    this.customOtherText.set('');
    this.searchQuery.set('');
    this.isDropdownOpen.set(false);
  }
}
