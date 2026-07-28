import { CompanyStatus } from './company-status.enum';

export interface CompanyResponse {
  id: string;
  name: string;
  cnpj: string;
  totalSpots: number;
  status: CompanyStatus;
}
