export type ModePaiement = 'ESPECES' | 'CHEQUE' | 'VIREMENT' | 'CARTE_BANCAIRE';
export type TypePaiement = 'AVANCE' | 'VERSEMENT';

export interface PaiementDTO {
  id: number;
  tenantId: number;
  candidatId: number;
  candidatNom?: string | null;
  candidatPrenom?: string | null;
  montant: number;
  modePaiement: ModePaiement;
  date: string;
  type: TypePaiement;
  recuUrl?: string | null;
}

export interface CreatePaiementRequest {
  candidatId: number;
  montant: number;
  modePaiement: ModePaiement;
  date: string;
  type: TypePaiement;
  recuUrl?: string | null;
}

export type CategorieCharge =
  | 'LOYER'
  | 'ASSURANCE'
  | 'CARBURANT'
  | 'ENTRETIEN'
  | 'SALAIRE'
  | 'FORMATIONS'
  | 'FOURNITURES'
  | 'AUTRE';

export type PeriodiciteCharge = 'PONCTUELLE' | 'MENSUELLE' | 'TRIMESTRIELLE' | 'ANNUELLE';

export interface ChargeDTO {
  id: number;
  tenantId: number;
  categorie: CategorieCharge;
  montant: number;
  date: string;
  periodicite: PeriodiciteCharge;
  justificatifUrl?: string | null;
}

export interface CreateChargeRequest {
  categorie: CategorieCharge;
  montant: number;
  date: string;
  periodicite: PeriodiciteCharge;
  justificatifUrl?: string | null;
}

export interface BalanceTresorerieDTO {
  dateDebut: string;
  dateFin: string;
  totalPaiements: number;
  totalCharges: number;
  soldeNet: number;
  nombrePaiements: number;
  nombreCharges: number;
  totalPaiementsAvance: number;
  totalPaiementsVersement: number;
}
