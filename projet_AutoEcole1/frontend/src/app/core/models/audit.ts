export type ActionAudit =
  | 'LOGIN'
  | 'LOGOUT'
  | 'CREATE_CANDIDAT'
  | 'UPDATE_CANDIDAT'
  | 'DELETE_CANDIDAT'
  | 'CREATE_PAIEMENT'
  | 'UPDATE_PAIEMENT'
  | 'DELETE_PAIEMENT'
  | 'CREATE_CHARGE'
  | 'UPDATE_CHARGE'
  | 'DELETE_CHARGE'
  | 'CREATE_SEANCE'
  | 'UPDATE_SEANCE'
  | 'DELETE_SEANCE'
  | 'VALIDER_SEANCE'
  | 'CONTESTER_SEANCE'
  | 'UPLOAD_DOCUMENT'
  | 'VALIDER_DOCUMENT'
  | 'REFUSER_DOCUMENT'
  | 'CREER_UTILISATEUR'
  | 'MODIFIER_UTILISATEUR'
  | 'DESACTIVER_UTILISATEUR';

export interface AuditLog {
  id: number;
  tenantId: number;
  utilisateurId: number;
  utilisateurNom?: string;
  action: ActionAudit;
  description: string;
  adresseIp?: string;
  userAgent?: string;
  dateCreation: string;
}
