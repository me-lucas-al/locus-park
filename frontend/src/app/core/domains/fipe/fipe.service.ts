import { Injectable } from '@angular/core';
import { FipeBrand, FipeModelsResponse } from './fipe.types';

const FIPE_BASE_URL = 'https://parallelum.com.br/fipe/api/v1';

@Injectable({ providedIn: 'root' })
export class FipeService {
  async fetchBrands(): Promise<FipeBrand[]> {
    const response = await fetch(`${FIPE_BASE_URL}/carros/marcas`);

    if (!response.ok) {
      throw new Error('Falha ao carregar a lista de marcas da tabela FIPE.');
    }

    return response.json() as Promise<FipeBrand[]>;
  }

  async fetchModelsByBrand(brandCode: string): Promise<FipeModelsResponse> {
    const response = await fetch(`${FIPE_BASE_URL}/carros/marcas/${brandCode}/modelos`);

    if (!response.ok) {
      throw new Error('Falha ao carregar os modelos para a marca selecionada.');
    }

    return response.json() as Promise<FipeModelsResponse>;
  }
}
