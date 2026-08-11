import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SearchableSelectComponent } from './searchable-select.component';
import { SelectOption } from '../../../core/domains/vehicle-catalog/vehicle-catalog.types';

describe('SearchableSelectComponent', () => {
  let component: SearchableSelectComponent;
  let fixture: ComponentFixture<SearchableSelectComponent>;

  const mockOptions: SelectOption[] = [
    { code: 'Chevrolet', label: 'Chevrolet' },
    { code: 'Fiat', label: 'Fiat' },
    { code: 'Ford', label: 'Ford' },
    { code: 'outro', label: 'Outro' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchableSelectComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SearchableSelectComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('options', mockOptions);
    fixture.detectChanges();
  });

  it('deve inicializar com valor vazio e dropdown fechado', () => {
    const inputEl = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    expect(inputEl.value).toBe('');
    expect(fixture.nativeElement.querySelector('.searchable-select-dropdown')).toBeNull();
  });

  it('deve selecionar uma opção e atualizar o input', () => {
    let emitted: SelectOption | undefined;
    component.optionSelected.subscribe((opt) => (emitted = opt));

    const inputEl = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    inputEl.dispatchEvent(new Event('focus'));
    fixture.detectChanges();

    const options = fixture.nativeElement.querySelectorAll('.dropdown-option');
    expect(options.length).toBe(4);

    (options[0] as HTMLElement).dispatchEvent(new MouseEvent('mousedown'));
    fixture.detectChanges();

    expect(emitted).toEqual({ code: 'Chevrolet', label: 'Chevrolet' });
    expect(inputEl.value).toBe('Chevrolet');
  });

  it('deve permitir apagar tudo do input sem voltar para a opção selecionada', () => {
    let lastEmitted: SelectOption | undefined;
    component.optionSelected.subscribe((opt) => (lastEmitted = opt));

    const inputEl = fixture.nativeElement.querySelector('input') as HTMLInputElement;

    // Seleciona Chevrolet
    inputEl.dispatchEvent(new Event('focus'));
    fixture.detectChanges();
    const options = fixture.nativeElement.querySelectorAll('.dropdown-option');
    (options[0] as HTMLElement).dispatchEvent(new MouseEvent('mousedown'));
    fixture.detectChanges();
    expect(inputEl.value).toBe('Chevrolet');

    // Usuário apaga tudo
    inputEl.value = '';
    inputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    // Deve emitir vazio e manter o input vazio (sem looping infinito)
    expect(inputEl.value).toBe('');
    expect(lastEmitted).toEqual({ code: '', label: '' });
  });

  it('deve filtrar as opções conforme o usuário digita', () => {
    const inputEl = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    inputEl.dispatchEvent(new Event('focus'));
    fixture.detectChanges();

    inputEl.value = 'Fi';
    inputEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const options = fixture.nativeElement.querySelectorAll('.dropdown-option');
    expect(options.length).toBe(1);
    expect(options[0].textContent?.trim()).toBe('Fiat');
  });
});
