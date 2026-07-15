export const VEICULOS = {
  branco: '#FFFFFF',
  creme: '#FFFDD0',
  prata: '#C0C0C0',
  cinza: '#708090',
  grafite: '#4F5D65',
  chumbo: '#374151',
  preto: '#1C1C1C',
  vermelho: '#D32F2F',
  vinho: '#58111A',
  rosa: '#FF69B4',
  roxo: '#4B0082',
  azul_claro: '#7EC8E3',
  azul_royal: '#0040FF',
  azul: '#1C3B57',
  ciano: '#00A896',
  verde_claro: '#A3E635',
  verde: '#1B4D3E',
  verde_militar: '#4B5320',
  bege: '#F5F5DC',
  champanhe: '#EEDC82',
  dourado: '#D4AF37',
  bronze: '#A87C43',
  marrom: '#5C4033',
  amarelo: '#F9A602',
  laranja: '#E65C00',
  outro: '#5A5A5A'
} as const;

export type VehicleColorKey = keyof typeof VEICULOS;

export const COLOR_LABELS: Record<VehicleColorKey, string> = {
  branco: 'Branco',
  creme: 'Creme',
  prata: 'Prata',
  cinza: 'Cinza',
  grafite: 'Grafite',
  chumbo: 'Chumbo',
  preto: 'Preto',
  vermelho: 'Vermelho',
  vinho: 'Vinho',
  rosa: 'Rosa',
  roxo: 'Roxo',
  azul_claro: 'Azul Claro',
  azul_royal: 'Azul Royal',
  azul: 'Azul',
  ciano: 'Ciano',
  verde_claro: 'Verde Claro',
  verde: 'Verde',
  verde_militar: 'Verde Militar',
  bege: 'Bege',
  champanhe: 'Champanhe',
  dourado: 'Dourado',
  bronze: 'Bronze',
  marrom: 'Marrom',
  amarelo: 'Amarelo',
  laranja: 'Laranja',
  outro: 'Outro'
};

export function getVehicleColorHex(colorName: string): string {
  const normalized = colorName.toLowerCase().trim().replace(/\s+/g, '_');
  if (normalized in VEICULOS) {
    return VEICULOS[normalized as VehicleColorKey];
  }
  return VEICULOS.outro;
}
