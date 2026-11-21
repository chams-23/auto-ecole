export interface VehiculeDTO {
  id: number;
  tenantId: number;
  immatriculation: string;
  categoriePermis: string;
  dateMiseService?: string | null;
  dateControleTechnique?: string | null;
  dateExpirationAssurance?: string | null;
  moniteurId?: number | null;
  moniteurNom?: string | null;
  moniteurPrenom?: string | null;
}

export interface VehiculeAlerteDTO {
  vehiculeId: number;
  immatriculation: string;
  typeAlerte: string;
  libelle: string;
  dateEvenement?: string | null;
  joursRestants?: number | null;
  niveau: 'CRITIQUE' | 'URGENT' | 'AVERTISSEMENT';
}

export interface CreateVehiculeRequest {
  immatriculation: string;
  categoriePermis: string;
  dateMiseService?: string | null;
  dateControleTechnique?: string | null;
  dateExpirationAssurance?: string | null;
  moniteurId?: number | null;
}
