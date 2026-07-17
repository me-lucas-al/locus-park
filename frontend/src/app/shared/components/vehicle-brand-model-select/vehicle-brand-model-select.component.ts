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
    return this.modelsQuery.data() ?? [];
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
