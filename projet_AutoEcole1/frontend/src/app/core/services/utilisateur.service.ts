import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UtilisateurDTO, CreateUtilisateurRequest } from '../models/user';

@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private apiUrl = `${environment.apiUrl}/utilisateurs`;

  constructor(private http: HttpClient) {}

  lister(): Observable<UtilisateurDTO[]> {
    return this.http.get<UtilisateurDTO[]>(this.apiUrl);
  }

  getById(id: number): Observable<UtilisateurDTO> {
    return this.http.get<UtilisateurDTO>(`${this.apiUrl}/${id}`);
  }

  creer(req: CreateUtilisateurRequest): Observable<UtilisateurDTO> {
    return this.http.post<UtilisateurDTO>(this.apiUrl, req);
  }

  modifier(id: number, dto: UtilisateurDTO): Observable<UtilisateurDTO> {
    return this.http.put<UtilisateurDTO>(`${this.apiUrl}/${id}`, dto);
  }

  desactiver(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
