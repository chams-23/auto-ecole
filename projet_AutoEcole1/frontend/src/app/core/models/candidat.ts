export interface CandidatDTO {
  id: number;
  utilisateurId: number;
  tenantId: number;
  cin: string;
  categoriePermis: string;
  statutParcours: string;
  forfaitId: number;
  heuresConduiteValidees: number;
  heuresTheorieValidees: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  dateExamenTheorie?: string | null;
  resultatExamenTheorie?: string | null;
  nombreTentativesTheorie?: number | null;
  dateExamenPratique?: string | null;
  resultatExamenPratique?: string | null;
  nombreTentativesPratique?: number | null;
}

export interface CompteurHeures {
  heuresConduiteValidees: number;
  heuresTheorieValidees: number;
  heuresConduiteRestantes: number;
  heuresTheorieRestantes: number;
}

export interface SoldeCandidat {
  totalDu: number;
  totalPaye: number;
  soldeRestant: number;
}

export interface StatutQuotaHeures {
  categoriePermis: string;
  quotaCodeMin: number;
  quotaConduiteMin: number;
  heuresCodeValidees: number;
  heuresConduiteValidees: number;
  codeAtteint: boolean;
  conduiteAtteint: boolean;
  quotaAtteint: boolean;
}

export interface CreateCandidatRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  password: string;
  cin: string;
  categoriePermis: string;
  forfaitId?: number | null;
}
