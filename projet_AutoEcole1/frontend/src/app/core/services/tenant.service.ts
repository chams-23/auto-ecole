import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  Tenant,
  CreateTenantRequest,
  StatutAbonnement,
  PlanAbonnement,
} from '../models/tenant';

@Injectable({ providedIn: 'root' })
export class TenantService {
  private apiUrl = `${environment.apiUrl}/tenants`;

  constructor(private http: HttpClient) {}

  listerTous(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(this.apiUrl);
  }

  creer(tenant: CreateTenantRequest): Observable<Tenant> {
    return this.http.post<Tenant>(this.apiUrl, tenant);
  }

  getById(id: number): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.apiUrl}/${id}`);
  }

  modifierStatutAbonnement(id: number, statut: StatutAbonnement): Observable<Tenant> {
    const params = new HttpParams().set('statut', statut);
    return this.http.patch<Tenant>(`${this.apiUrl}/${id}/statut-abonnement`, null, { params });
  }

  activerAbonnement(id: number): Observable<Tenant> {
    return this.http.patch<Tenant>(`${this.apiUrl}/${id}/activer`, {});
  }

  suspendreAbonnement(id: number, dateSuspension: string): Observable<Tenant> {
    const params = new HttpParams().set('dateSuspension', dateSuspension);
    return this.http.patch<Tenant>(`${this.apiUrl}/${id}/suspendre`, null, { params });
  }

  resilierAbonnement(id: number, dateResiliation: string): Observable<Tenant> {
    const params = new HttpParams().set('dateResiliation', dateResiliation);
    return this.http.patch<Tenant>(`${this.apiUrl}/${id}/resilier`, null, { params });
  }

  modifierPlanAbonnement(
    id: number,
    plan: PlanAbonnement,
    debut: string,
    fin: string,
    montant: number
  ): Observable<Tenant> {
    const params = new HttpParams()
      .set('plan', plan)
      .set('debut', debut)
      .set('fin', fin)
      .set('montant', montant.toString());
    return this.http.patch<Tenant>(`${this.apiUrl}/${id}/plan`, null, { params });
  }
}
