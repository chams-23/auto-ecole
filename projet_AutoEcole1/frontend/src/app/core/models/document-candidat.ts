export type TypeDocumentCandidat =
  | 'CNE'
  | 'CIN_COPIE'
  | 'CERTIFICAT_RESIDENCE'
  | 'CERTIFICAT_SCOLARITE'
  | 'CERTIFICAT_MEDICAL'
  | 'PHOTO_IDENTITE'
  | 'ATTESTATION_INSCRIPTION'
  | 'PERMIS_PROVISOIRE'
  | 'AUTRE';

export type StatutValidationDocument =
  | 'EN_ATTENTE'
  | 'VALIDE'
  | 'REFUSE'
  | 'EXPIRIE';

export interface DocumentCandidatDTO {
  id: number;
  candidatId: number;
  tenantId: number;
  typeDocument: TypeDocumentCandidat;
  nomFichier: string;
  urlFichier?: string | null;
  statut: StatutValidationDocument;
  dateUpload: string;
  dateExpiration?: string | null;
  commentaire?: string | null;
  uploadParUtilisateurId?: number | null;
  valideParUtilisateurId?: number | null;
  dateValidation?: string | null;
}

export interface UploadDocumentCandidatRequest {
  typeDocument: TypeDocumentCandidat;
  nomFichier: string;
  urlFichier?: string | null;
  base64Content?: string | null;
  dateExpiration?: string | null;
}

export interface ValidationDocumentRequest {
  commentaire?: string | null;
}
