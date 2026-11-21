export type StatutAbonnement = 'ACTIF' | 'SUSPENDU' | 'RESILIE';
export type PlanAbonnement = 'ESSENTIEL' | 'PROFESSIONNEL' | 'PREMIUM';

export interface Tenant {
  id: number;
  raisonSociale: string;
  numeroAgrement: string;
  wilaya: string;
  statutAbonnement: StatutAbonnement;
  planAbonnement?: PlanAbonnement | null;
  dateDebutAbonnement?: string | null;
  dateFinAbonnement?: string | null;
  montantAbonnement?: number | null;
  createdAt?: string | null;
}

export interface CreateTenantRequest {
  raisonSociale: string;
  numeroAgrement: string;
  wilaya: string;
  planAbonnement: PlanAbonnement;
  dateDebutAbonnement: string;
  dateFinAbonnement: string;
  montantAbonnement: number;
}
