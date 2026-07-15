import { Component, Input, Output, EventEmitter, signal, computed, HostListener, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { COLOR_LABELS, VEICULOS, VehicleColorKey } from '../../utils/colors';

@Component({
  selector: 'app-color-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './color-select.component.html',
  styleUrl: './color-select.component.css'
})
export class ColorSelect {
  private readonly elementRef = inject(ElementRef);

  @Input() selectedColor: string = 'outro';
  @Output() readonly colorChange = new EventEmitter<string>();

  protected readonly isOpen = signal(false);
  protected readonly searchQuery = signal('');

  protected readonly selectedLabel = computed(() => {
    const key = this.selectedColor.toLowerCase() as VehicleColorKey;
    return COLOR_LABELS[key] || this.selectedColor || 'Outro';
  });

  protected readonly selectedHex = computed(() => {
    const key = this.selectedColor.toLowerCase() as VehicleColorKey;
    return VEICULOS[key] || VEICULOS.outro;
  });

  protected readonly filteredOptions = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const all = Object.keys(VEICULOS).map((key) => ({
      key,
      label: COLOR_LABELS[key as VehicleColorKey],
      hex: VEICULOS[key as VehicleColorKey],
    }));

    if (!query) return all;
    return all.filter((opt) => opt.label.toLowerCase().includes(query));
  });

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen.set(false);
    }
  }

  protected toggleDropdown(): void {
    this.isOpen.set(!this.isOpen());
    if (this.isOpen()) {
      this.searchQuery.set('');
    }
  }

  protected selectOption(key: string): void {
    this.selectedColor = key;
    this.colorChange.emit(key);
    this.isOpen.set(false);
  }
}
