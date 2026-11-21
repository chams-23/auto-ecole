import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuditLog, ActionAudit } from '../models/audit';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  lister(params?: {
    action?: ActionAudit;
    utilisateurId?: number;
    dateDebut?: string;
    dateFin?: string;
    page?: number;
    size?: number;
  }): Observable<AuditLog[]> {
    let httpParams = new HttpParams();
    if (params?.action) httpParams = httpParams.set('action', params.action);
    if (params?.utilisateurId) httpParams = httpParams.set('utilisateurId', params.utilisateurId);
    if (params?.dateDebut) httpParams = httpParams.set('dateDebut', params.dateDebut);
    if (params?.dateFin) httpParams = httpParams.set('dateFin', params.dateFin);
    if (params?.page) httpParams = httpParams.set('page', params.page);
    if (params?.size) httpParams = httpParams.set('size', params.size);
    return this.http.get<AuditLog[]>(this.apiUrl, { params: httpParams });
  }

  getById(id: number): Observable<AuditLog> {
    return this.http.get<AuditLog>(`${this.apiUrl}/${id}`);
  }
}
