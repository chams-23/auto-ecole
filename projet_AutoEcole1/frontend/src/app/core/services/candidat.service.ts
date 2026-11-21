import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CandidatDTO,
  CompteurHeures,
  SoldeCandidat,
  StatutQuotaHeures,
} from '../models/candidat';

@Injectable({ providedIn: 'root' })
export class CandidatService {
  private apiUrl = `${environment.apiUrl}/candidats`;

  constructor(private http: HttpClient) {}

  lister(): Observable<CandidatDTO[]> {
    return this.http.get<CandidatDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<CandidatDTO> {
    return this.http.get<CandidatDTO>(`${this.apiUrl}/${id}`);
  }

  creer(candidat: any): Observable<CandidatDTO> {
    return this.http.post<CandidatDTO>(this.apiUrl, candidat);
  }

  modifier(id: number, data: any): Observable<CandidatDTO> {
    return this.http.patch<CandidatDTO>(`${this.apiUrl}/${id}`, data);
  }

  compteurHeures(id: number): Observable<CompteurHeures> {
    return this.http.get<CompteurHeures>(`${this.apiUrl}/${id}/compteur-heures`);
  }

  solde(id: number): Observable<SoldeCandidat> {
    return this.http.get<SoldeCandidat>(`${this.apiUrl}/${id}/solde`);
  }

  statutQuota(id: number): Observable<StatutQuotaHeures> {
    return this.http.get<StatutQuotaHeures>(`${this.apiUrl}/${id}/statut-quota`);
  }
}
