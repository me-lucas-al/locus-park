import {
  Component,
  signal,
  computed,
  output,
  viewChild,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchableSelectComponent } from '../searchable-select/searchable-select.component';
import { useFipeBrandsQuery, useFipeModelsByBrandQuery } from '../../../core/domains/fipe/fipe.hooks';
import { SelectOption, OUTRO_OPTION } from '../../../core/domains/fipe/fipe.types';
import ambiguidades from '../../../core/domains/fipe/ambiguidades-revisao.json';

@Component({
  selector: 'app-vehicle-brand-model-select',
  standalone: true,
  imports: [CommonModule, SearchableSelectComponent],
  templateUrl: './vehicle-brand-model-select.component.html',
  styleUrl: './vehicle-brand-model-select.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VehicleBrandModelSelectComponent {
  readonly brandSelected = output<SelectOption>();
  readonly modelSelected = output<SelectOption>();

  private readonly brandSelectRef = viewChild<SearchableSelectComponent>('brandSelect');
  private readonly modelSelectRef = viewChild<SearchableSelectComponent>('modelSelect');

  protected readonly selectedBrandCode = signal('');

  protected readonly brandsQuery = useFipeBrandsQuery();
  protected readonly modelsQuery = useFipeModelsByBrandQuery(this.selectedBrandCode);

  protected readonly isModelSelectDisabled = computed(() => !this.selectedBrandCode());

  protected readonly modelOptions = computed<SelectOption[]>(() => {
    if (this.selectedBrandCode() === OUTRO_OPTION.code) return [OUTRO_OPTION];
    
    const models = this.modelsQuery.data() ?? [];
    if (models.length === 0) return [];

    const brands = this.brandsQuery.data() ?? [];
    const selectedBrand = brands.find(b => b.code === this.selectedBrandCode());
    const brandName = selectedBrand?.label;

    if (!brandName) return models;

    const rules = ambiguidades.filter(
      item => item.marca.toLowerCase() === brandName.toLowerCase()
    );

    if (rules.length === 0) return models;

    const cleanedModels = models.map(model => {
      const rule = rules.find(
        r => r.original.toLowerCase() === model.label.toLowerCase()
      );
      return rule ? { ...model, label: rule.limpo } : model;
    });

    const uniqueModels: SelectOption[] = [];
    const seenLabels = new Set<string>();

    for (const m of cleanedModels) {
      const normalizedLabel = m.label.toLowerCase().trim();
      if (!seenLabels.has(normalizedLabel)) {
        seenLabels.add(normalizedLabel);
        uniqueModels.push(m);
      }
    }

    return uniqueModels;
  });

  protected readonly brandsError = computed(() =>
    this.brandsQuery.isError() ? 'Erro ao carregar marcas. Verifique sua conexão.' : null
  );

  protected readonly modelsError = computed(() =>
    this.modelsQuery.isError() ? 'Erro ao carregar modelos. Tente novamente.' : null
  );

  protected onBrandSelected(brand: SelectOption): void {
    const isSameBrand = this.selectedBrandCode() === brand.code;
    this.selectedBrandCode.set(brand.code);
    if (!isSameBrand) this.modelSelectRef()?.reset();
    this.brandSelected.emit(brand);
  }

  protected onModelSelected(model: SelectOption): void {
    this.modelSelected.emit(model);
  }

  reset(): void {
    this.selectedBrandCode.set('');
    this.brandSelectRef()?.reset();
    this.modelSelectRef()?.reset();
  }
}
