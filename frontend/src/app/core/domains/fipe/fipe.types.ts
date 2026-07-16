export interface FipeBrand {
  codigo: string;
  nome: string;
}

export interface FipeModel {
  codigo: number;
  nome: string;
}

export interface FipeModelsResponse {
  modelos: FipeModel[];
  anos: unknown[];
}

export interface SelectOption {
  code: string;
  label: string;
}
