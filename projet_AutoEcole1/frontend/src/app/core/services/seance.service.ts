import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SeanceDTO,
  CreateSeanceRequest,
  ReprogrammerSeanceRequest,
  DeclarerEffectueeRequest,
} from '../models/seance';

@Injectable({ providedIn: 'root' })
export class SeanceService {
  private apiUrl = `${environment.apiUrl}/seances`;

  constructor(private http: HttpClient) {}

  filtrer(params?: { date?: string; moniteur_id?: number; candidat_id?: number }): Observable<SeanceDTO[]> {
    let httpParams = new HttpParams();
    if (params?.date) httpParams = httpParams.set('date', params.date);
    if (params?.moniteur_id) httpParams = httpParams.set('moniteur_id', params.moniteur_id);
    if (params?.candidat_id) httpParams = httpParams.set('candidat_id', params.candidat_id);
    return this.http.get<SeanceDTO[]>(this.apiUrl, { params: httpParams });
  }

  creer(req: CreateSeanceRequest): Observable<SeanceDTO> {
    return this.http.post<SeanceDTO>(this.apiUrl, req);
  }

  reprogrammer(id: number, req: ReprogrammerSeanceRequest): Observable<SeanceDTO> {
    return this.http.patch<SeanceDTO>(`${this.apiUrl}/${id}/reprogrammer`, req);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  declarerEffectuee(id: number, req?: DeclarerEffectueeRequest): Observable<SeanceDTO> {
    return this.http.post<SeanceDTO>(`${this.apiUrl}/${id}/declarer-effectuee`, req || {});
  }

  valider(id: number): Observable<SeanceDTO> {
    return this.http.post<SeanceDTO>(`${this.apiUrl}/${id}/valider`, {});
  }

  contester(id: number, observations?: string): Observable<SeanceDTO> {
    return this.http.post<SeanceDTO>(`${this.apiUrl}/${id}/contester`, observations ? { observations } : {});
  }
}
