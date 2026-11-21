/**
 * Matrice RBAC CDC §3.1 — droits par rôle.
 * Source de vérité côté frontend pour navigation et actions UI.
 */
export type AppRole = 'SUPER_ADMIN' | 'ADMIN' | 'SECRETAIRE' | 'MONITEUR' | 'CANDIDAT';

export const RBAC = {
  /** Créer / configurer l'auto-école (tenant) */
  configurerTenant: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Gérer les comptes utilisateurs */
  gererUtilisateurs: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Attribuer un véhicule à un moniteur */
  attribuerVehicule: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Consulter la comptabilité générale */
  comptabiliteGenerale: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Paramétrer les tarifs / formules */
  parametrerTarifs: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Inscrire / modifier un candidat */
  gererCandidats: ['ADMIN', 'SECRETAIRE', 'SUPER_ADMIN'] as AppRole[],
  /** Consulter dossiers candidats (lecture) */
  voirCandidats: ['ADMIN', 'SECRETAIRE', 'MONITEUR', 'SUPER_ADMIN'] as AppRole[],
  /** Créer / modifier le planning */
  gererPlanning: ['ADMIN', 'SECRETAIRE', 'SUPER_ADMIN'] as AppRole[],
  /** Consulter son planning personnel */
  voirPlanningPersonnel: ['MONITEUR', 'CANDIDAT'] as AppRole[],
  /** Voir planning (tous rôles concernés) */
  voirPlanning: ['ADMIN', 'SECRETAIRE', 'MONITEUR', 'CANDIDAT', 'SUPER_ADMIN'] as AppRole[],
  /** Saisir un paiement */
  saisirPaiement: ['ADMIN', 'SECRETAIRE', 'SUPER_ADMIN'] as AppRole[],
  /** Consulter solde / reste à payer */
  voirSolde: ['ADMIN', 'SECRETAIRE', 'CANDIDAT', 'SUPER_ADMIN'] as AppRole[],
  /** Saisir une charge de trésorerie */
  saisirCharge: ['ADMIN', 'SECRETAIRE', 'SUPER_ADMIN'] as AppRole[],
  /** Générer les états financiers */
  etatsFinanciers: ['ADMIN', 'SECRETAIRE', 'SUPER_ADMIN'] as AppRole[],
  /** Déclarer / valider séance de conduite (côté moniteur) */
  validerConduite: ['MONITEUR', 'CANDIDAT'] as AppRole[],
  /** Déclarer / valider séance de théorie (côté secrétaire) */
  validerTheorie: ['SECRETAIRE', 'CANDIDAT'] as AppRole[],
  /** Historique des heures */
  historiqueHeures: ['MONITEUR', 'CANDIDAT'] as AppRole[],
  /** Notifications */
  notifications: ['SECRETAIRE', 'MONITEUR', 'CANDIDAT', 'ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Parc véhicules — écriture */
  gererVehicules: ['ADMIN', 'SUPER_ADMIN'] as AppRole[],
  /** Véhicules — lecture (moniteur : véhicule attribué) */
  voirVehicules: ['ADMIN', 'SUPER_ADMIN', 'MONITEUR'] as AppRole[],
} as const;

export function roleIn(role: AppRole | null | undefined, allowed: readonly AppRole[]): boolean {
  return !!role && allowed.includes(role);
}
