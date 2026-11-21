import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PaiementDTO,
  CreatePaiementRequest,
  ChargeDTO,
  CreateChargeRequest,
  BalanceTresorerieDTO,
} from '../models/tresorerie';

@Injectable({ providedIn: 'root' })
export class TresorerieService {
  private paiementsUrl = `${environment.apiUrl}/paiements`;
  private chargesUrl = `${environment.apiUrl}/charges`;
  private balanceUrl = `${environment.apiUrl}/tresorerie/balance`;
  private exportsUrl = `${environment.apiUrl}/exports`;

  constructor(private http: HttpClient) {}

  listerPaiements(candidatId?: number): Observable<PaiementDTO[]> {
    let params = new HttpParams();
    if (candidatId) params = params.set('candidat_id', candidatId);
    return this.http.get<PaiementDTO[]>(this.paiementsUrl, { params });
  }

  creerPaiement(req: CreatePaiementRequest): Observable<PaiementDTO> {
    return this.http.post<PaiementDTO>(this.paiementsUrl, req);
  }

  getRecuPaiement(id: number): Observable<{ recuUrl: string }> {
    return this.http.get<{ recuUrl: string }>(`${this.paiementsUrl}/${id}/recu`);
  }

  listerCharges(): Observable<ChargeDTO[]> {
    return this.http.get<ChargeDTO[]>(this.chargesUrl);
  }

  creerCharge(req: CreateChargeRequest): Observable<ChargeDTO> {
    return this.http.post<ChargeDTO>(this.chargesUrl, req);
  }

  getBalance(periode: 'SEMAINE' | 'MOIS' | 'TRIMESTRE' | 'ANNEE' = 'MOIS'): Observable<BalanceTresorerieDTO> {
    const params = new HttpParams().set('periode', periode);
    return this.http.get<BalanceTresorerieDTO>(this.balanceUrl, { params });
  }

  exporterComptable(debut: string, fin: string): Observable<HttpResponse<Blob>> {
    const params = new HttpParams().set('debut', debut).set('fin', fin);
    return this.http.get(`${this.exportsUrl}/comptable.xlsx`, {
      params,
      observe: 'response',
      responseType: 'blob',
    });
  }
}
