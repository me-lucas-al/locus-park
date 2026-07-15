export interface VehicleRequest {
  plate: string;
  model: string;
  color: string;
  type: 'CAR' | 'MOTORCYCLE' | 'VAN' | 'TRUCK';
  clientId?: string;
}

export interface VehicleResponse {
  id: string;
  plate: string;
  model: string;
  color: string;
  type: 'CAR' | 'MOTORCYCLE' | 'VAN' | 'TRUCK';
  clientId?: string;
  companyId: string;
}

export interface UpdateVehicleParams {
  companyId: string;
  id: string;
  request: VehicleRequest;
}

