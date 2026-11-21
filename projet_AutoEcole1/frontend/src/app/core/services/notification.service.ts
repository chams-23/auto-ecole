import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationDTO } from '../models/notification';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  lister(): Observable<NotificationDTO[]> {
    return this.http.get<NotificationDTO[]>(this.apiUrl);
  }

  marquerLue(id: number): Observable<NotificationDTO> {
    return this.http.patch<NotificationDTO>(`${this.apiUrl}/${id}/lue`, {});
  }

  toutMarquerLue(): Observable<{ marquees: number; success: boolean }> {
    return this.http.patch<{ marquees: number; success: boolean }>(`${this.apiUrl}/tout-lire`, {});
  }
}
