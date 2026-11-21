import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VehiculeDTO, VehiculeAlerteDTO, CreateVehiculeRequest } from '../models/vehicule';

@Injectable({ providedIn: 'root' })
export class VehiculeService {
  private apiUrl = `${environment.apiUrl}/vehicules`;

  constructor(private http: HttpClient) {}

  lister(): Observable<VehiculeDTO[]> {
    return this.http.get<VehiculeDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<VehiculeDTO> {
    return this.http.get<VehiculeDTO>(`${this.apiUrl}/${id}`);
  }

  creer(req: CreateVehiculeRequest): Observable<VehiculeDTO> {
    return this.http.post<VehiculeDTO>(this.apiUrl, req);
  }

  modifier(id: number, dto: Partial<VehiculeDTO>): Observable<VehiculeDTO> {
    return this.http.patch<VehiculeDTO>(`${this.apiUrl}/${id}`, dto);
  }

  attribuerMoniteur(id: number, moniteurId: number | null): Observable<VehiculeDTO> {
    return this.http.patch<VehiculeDTO>(`${this.apiUrl}/${id}/attribuer-moniteur`, { moniteurId });
  }

  getAlertes(): Observable<VehiculeAlerteDTO[]> {
    return this.http.get<VehiculeAlerteDTO[]>(`${this.apiUrl}/alertes`);
  }
}
