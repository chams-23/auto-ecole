export interface QuotaHeuresDTO {
  id: number;
  tenantId: number;
  categoriePermis: string;
  heuresCodeMin: number;
  heuresConduiteMin: number;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface UpsertQuotaHeuresRequest {
  categoriePermis: string;
  heuresCodeMin?: number | null;
  heuresConduiteMin?: number | null;
}

export const CATEGORIES_PERMIS: { v: string; l: string; defaultCode: number; defaultConduite: number }[] = [
  { v: 'B',  l: 'Permis B (véhicules légers)',     defaultCode: 60, defaultConduite: 20 },
  { v: 'A',  l: 'Permis A (moto ≥ 125cm³)',        defaultCode: 30, defaultConduite: 15 },
  { v: 'A1', l: 'Permis A1 (moto ≤ 125cm³)',       defaultCode: 20, defaultConduite: 8  },
  { v: 'A2', l: 'Permis A2 (moto ≤ 35kW)',          defaultCode: 25, defaultConduite: 12 },
  { v: 'C',  l: 'Permis C (poids lourds)',          defaultCode: 80, defaultConduite: 40 },
  { v: 'D',  l: 'Permis D (transport en commun)',   defaultCode: 100, defaultConduite: 50 },
  { v: 'EB', l: 'Permis EB (remorque B)',           defaultCode: 60, defaultConduite: 20 },
  { v: 'EC', l: 'Permis EC (remorque C)',           defaultCode: 80, defaultConduite: 40 },
  { v: 'ED', l: 'Permis ED (remorque D)',           defaultCode: 100, defaultConduite: 50 },
];
