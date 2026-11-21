export type StatutValidation =
  | 'EN_ATTENTE'
  | 'DECLAREE_EFFECTUEE'
  | 'VALIDEE'
  | 'CONTESTEE'
  | 'VERROUILLEE';

export type TypeSeance = 'THEORIE' | 'CONDUITE';

export interface SeanceDTO {
  id: number;
  tenantId: number;
  type: TypeSeance;
  date: string;
  heureDebut: string;
  heureFin: string;
  candidatId: number;
  candidatNom?: string | null;
  candidatPrenom?: string | null;
  intervenantId: number;
  intervenantNom?: string | null;
  intervenantPrenom?: string | null;
  statutValidation: StatutValidation;
  kilometrage?: number | null;
  observations?: string | null;
}

export interface CreateSeanceRequest {
  type: TypeSeance;
  date: string;
  heureDebut: string;
  heureFin: string;
  candidatId: number;
  intervenantId: number;
  kilometrage?: number | null;
  observations?: string | null;
}

export interface ReprogrammerSeanceRequest {
  date?: string | null;
  heureDebut?: string | null;
  heureFin?: string | null;
  type?: TypeSeance | null;
  intervenantId?: number | null;
}

export interface DeclarerEffectueeRequest {
  kilometrage?: number | null;
  observations?: string | null;
}
