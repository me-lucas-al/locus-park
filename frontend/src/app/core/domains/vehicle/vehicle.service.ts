import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { VehicleRequest, VehicleResponse } from './vehicle.types';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}vehicles`;

  create(request: VehicleRequest): Observable<VehicleResponse> {
    return this.http.post<VehicleResponse>(this.baseUrl, request);
  }

  listAll(): Observable<VehicleResponse[]> {
    return this.http.get<VehicleResponse[]>(this.baseUrl);
  }

  getById(id: string): Observable<VehicleResponse> {
    return this.http.get<VehicleResponse>(`${this.baseUrl}/${id}`);
  }

  update(id: string, request: VehicleRequest): Observable<VehicleResponse> {
    return this.http.put<VehicleResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
