import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { QuotaHeuresDTO, UpsertQuotaHeuresRequest } from '../models/quota-heures';

@Injectable({ providedIn: 'root' })
export class QuotaHeuresService {
  private apiUrl = `${environment.apiUrl}/quotas-heures`;

  constructor(private http: HttpClient) {}

  lister(): Observable<QuotaHeuresDTO[]> {
    return this.http.get<QuotaHeuresDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<QuotaHeuresDTO> {
    return this.http.get<QuotaHeuresDTO>(`${this.apiUrl}/${id}`);
  }

  creer(req: UpsertQuotaHeuresRequest): Observable<QuotaHeuresDTO> {
    return this.http.post<QuotaHeuresDTO>(this.apiUrl, req);
  }

  modifier(id: number, req: UpsertQuotaHeuresRequest): Observable<QuotaHeuresDTO> {
    return this.http.put<QuotaHeuresDTO>(`${this.apiUrl}/${id}`, req);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
