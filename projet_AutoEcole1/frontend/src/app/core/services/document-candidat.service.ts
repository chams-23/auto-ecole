import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DocumentCandidatDTO,
  UploadDocumentCandidatRequest,
  ValidationDocumentRequest,
} from '../models/document-candidat';

@Injectable({ providedIn: 'root' })
export class DocumentCandidatService {
  private baseApi = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listerParCandidat(candidatId: number): Observable<DocumentCandidatDTO[]> {
    return this.http.get<DocumentCandidatDTO[]>(
      `${this.baseApi}/candidats/${candidatId}/documents`
    );
  }

  uploaderDocument(
    candidatId: number,
    request: UploadDocumentCandidatRequest
  ): Observable<DocumentCandidatDTO> {
    return this.http.post<DocumentCandidatDTO>(
      `${this.baseApi}/candidats/${candidatId}/documents`,
      request
    );
  }

  getById(id: number): Observable<DocumentCandidatDTO> {
    return this.http.get<DocumentCandidatDTO>(`${this.baseApi}/documents/${id}`);
  }

  validerDocument(
    id: number,
    request?: ValidationDocumentRequest
  ): Observable<DocumentCandidatDTO> {
    return this.http.patch<DocumentCandidatDTO>(
      `${this.baseApi}/documents/${id}/valider`,
      request ?? {}
    );
  }

  refuserDocument(
    id: number,
    request?: ValidationDocumentRequest
  ): Observable<DocumentCandidatDTO> {
    return this.http.patch<DocumentCandidatDTO>(
      `${this.baseApi}/documents/${id}/refuser`,
      request ?? {}
    );
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApi}/documents/${id}`);
  }
}
