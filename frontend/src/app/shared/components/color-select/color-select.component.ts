import { Component, model, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-color-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './color-select.component.html',
  styleUrl: './color-select.component.css',
})
export class ColorSelectComponent {
  readonly color = model<string>('branco');
  readonly customColor = model<string>('');

  readonly colorSearchQuery = signal<string>('Branco');
  readonly colorDropdownOpen = signal<boolean>(false);

  readonly VEICULOS_CORES = {
    branco: { hex: '#FFFFFF', label: 'Branco' },
    creme: { hex: '#FFFDD0', label: 'Creme' },
    prata: { hex: '#C0C0C0', label: 'Prata' },
    cinza: { hex: '#708090', label: 'Cinza' },
    grafite: { hex: '#4F5D65', label: 'Grafite' },
    chumbo: { hex: '#374151', label: 'Chumbo' },
    preto: { hex: '#1C1C1C', label: 'Preto' },
    vermelho: { hex: '#D32F2F', label: 'Vermelho' },
    vinho: { hex: '#58111A', label: 'Vinho' },
    rosa: { hex: '#FF69B4', label: 'Rosa' },
    roxo: { hex: '#4B0082', label: 'Roxo' },
    azul_claro: { hex: '#7EC8E3', label: 'Azul Claro' },
    azul_royal: { hex: '#0040FF', label: 'Azul Royal' },
    azul: { hex: '#1C3B57', label: 'Azul' },
    ciano: { hex: '#00A896', label: 'Ciano' },
    verde_claro: { hex: '#A3E635', label: 'Verde Claro' },
    verde: { hex: '#1B4D3E', label: 'Verde' },
    verde_militar: { hex: '#4B5320', label: 'Verde Militar' },
    bege: { hex: '#F5F5DC', label: 'Bege' },
    champanhe: { hex: '#EEDC82', label: 'Champanhe' },
    dourado: { hex: '#D4AF37', label: 'Dourado' },
    bronze: { hex: '#A87C43', label: 'Bronze' },
    marrom: { hex: '#5C4033', label: 'Marrom' },
    amarelo: { hex: '#F9A602', label: 'Amarelo' },
    laranja: { hex: '#E65C00', label: 'Laranja' },
    outro: { hex: '#5A5A5A', label: 'Outro' }
  };

  readonly filteredColors = computed(() => {
    const query = this.colorSearchQuery().toLowerCase().trim();
    const list = Object.entries(this.VEICULOS_CORES).map(([key, info]) => ({
      key,
      hex: info.hex,
      label: info.label
    }));

    if (!query) return list;

    return list.filter(item => item.label.toLowerCase().includes(query));
  });

  protected toggleDropdown(): void {
    this.colorDropdownOpen.update(v => !v);
  }

  protected openDropdown(): void {
    this.colorDropdownOpen.set(true);
    this.colorSearchQuery.set('');
  }

  protected closeDropdown(): void {
    setTimeout(() => {
      this.colorDropdownOpen.set(false);
      
      const active = this.color();
      if (!this.colorSearchQuery()) {
        const info = (this.VEICULOS_CORES as any)[active];
        this.colorSearchQuery.set(info ? info.label : 'Branco');
      }
    }, 200);
  }

  protected onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.colorSearchQuery.set(input.value);
  }

  protected selectOption(colorKey: string): void {
    this.color.set(colorKey);
    const info = (this.VEICULOS_CORES as any)[colorKey];
    this.colorSearchQuery.set(info ? info.label : '');
    this.colorDropdownOpen.set(false);
  }

  protected getColorHex(colorKey: string): string {
    const info = (this.VEICULOS_CORES as any)[colorKey];
    return info ? info.hex : '#FFFFFF';
  }

  protected onCustomColorInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.customColor.set(input.value);
  }

  reset(): void {
    this.color.set('branco');
    this.customColor.set('');
    this.colorSearchQuery.set('Branco');
    this.colorDropdownOpen.set(false);
  }
}
