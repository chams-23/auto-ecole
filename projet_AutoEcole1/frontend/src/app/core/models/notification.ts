export type TypeEvenementNotification =
  | 'NOUVELLE_SEANCE'
  | 'SEANCE_REPROGRAMMEE'
  | 'SEANCE_DECLAREE'
  | 'SEANCE_VALIDEE'
  | 'SEANCE_CONTESTEE'
  | 'SEANCE_A_VALIDER'
  | 'PAIEMENT_CREE'
  | 'PAIEMENT_EN_RETARD'
  | 'CHARGE_CREE'
  | 'DOCUMENT_BIENTOT_EXPIRE'
  | 'CANDIDAT_ADMIS'
  | 'ALERTE_VEHICULE'
  | 'UTILISATEUR_CREE'
  | 'MESSAGE_SYSTEME';

export interface NotificationDTO {
  id: number;
  destinataireId: number;
  typeEvenement: TypeEvenementNotification;
  contenu: string;
  statutLecture: boolean;
  date: string;
}
