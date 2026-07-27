import { VehicleType } from '../../core/types/domain-enums.types';

export const VEHICLE_TYPE_LABELS: Record<VehicleType, string> = {
  CAR: 'Carro',
  MOTORCYCLE: 'Moto',
  VAN: 'Van',
  TRUCK: 'Caminhão',
};

export function vehicleTypeLabel(type: VehicleType): string {
  return VEHICLE_TYPE_LABELS[type];
}
