import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationDTO, TypeEvenementNotification } from '../../core/models/notification';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './notifications.component.html',
})
export class NotificationsComponent implements OnInit {
  notifications: NotificationDTO[] = [];
  searchTerm = '';
  filtreType: TypeEvenementNotification | '' = '';
  filtreStatut: 'TOUTES' | 'NON_LUES' | 'LUES' = 'TOUTES';
  loading = false;

  notificationsMock: NotificationDTO[] = [
    { id: 1, destinataireId: 1, typeEvenement: 'NOUVELLE_SEANCE', contenu: 'Nouvelle séance de conduite programmée le 15/08/2026 à 10h00 avec le moniteur Ahmed.', statutLecture: false, date: new Date(Date.now() - 3600000).toISOString() },
    { id: 2, destinataireId: 1, typeEvenement: 'SEANCE_VALIDEE', contenu: 'Votre séance du 10/08/2026 a été validée. 2h ajoutées à votre compteur.', statutLecture: false, date: new Date(Date.now() - 7200000 * 5).toISOString() },
    { id: 3, destinataireId: 1, typeEvenement: 'PAIEMENT_CREE', contenu: 'Paiement de 1500 DH reçu avec succès. Merci pour votre règlement.', statutLecture: true, date: new Date(Date.now() - 86400000 * 2).toISOString() },
    { id: 4, destinataireId: 1, typeEvenement: 'SEANCE_CONTESTEE', contenu: 'La séance du 12/08/2026 a été contestée. Veuillez contacter l\'administration.', statutLecture: false, date: new Date(Date.now() - 86400000 * 3).toISOString() },
    { id: 5, destinataireId: 1, typeEvenement: 'DOCUMENT_BIENTOT_EXPIRE', contenu: 'Votre pièce d\'identité expire dans 30 jours. Pensez à la renouveler.', statutLecture: true, date: new Date(Date.now() - 86400000 * 5).toISOString() },
    { id: 6, destinataireId: 1, typeEvenement: 'CANDIDAT_ADMIS', contenu: 'Félicitations ! Vous avez été déclaré admis à l\'examen du permis B.', statutLecture: false, date: new Date(Date.now() - 86400000 * 7).toISOString() },
    { id: 7, destinataireId: 1, typeEvenement: 'ALERTE_VEHICULE', contenu: 'Alerte véhicule : Vidange à prévoir pour le véhicule immatriculé 12345-A-7.', statutLecture: true, date: new Date(Date.now() - 86400000 * 10).toISOString() },
    { id: 8, destinataireId: 1, typeEvenement: 'PAIEMENT_EN_RETARD', contenu: 'Attention : Un paiement de 2000 DH est en retard. Merci de régler rapidement.', statutLecture: false, date: new Date(Date.now() - 86400000 * 12).toISOString() },
    { id: 9, destinataireId: 1, typeEvenement: 'SEANCE_REPROGRAMMEE', contenu: 'Séance du 18/08/2026 reportée au 20/08/2026 à 14h00.', statutLecture: true, date: new Date(Date.now() - 86400000 * 15).toISOString() },
    { id: 10, destinataireId: 1, typeEvenement: 'MESSAGE_SYSTEME', contenu: 'Mise à jour du logiciel prévue ce soir à 23h. Interruption de service estimée à 30 minutes.', statutLecture: true, date: new Date(Date.now() - 86400000 * 20).toISOString() },
  ];

  typesEvenements: { v: TypeEvenementNotification; l: string }[] = [
    { v: 'NOUVELLE_SEANCE', l: 'Nouvelle séance' },
    { v: 'SEANCE_REPROGRAMMEE', l: 'Séance reprogrammée' },
    { v: 'SEANCE_DECLAREE', l: 'Séance déclarée' },
    { v: 'SEANCE_VALIDEE', l: 'Séance validée' },
    { v: 'SEANCE_CONTESTEE', l: 'Séance contestée' },
    { v: 'SEANCE_A_VALIDER', l: 'Séance à valider' },
    { v: 'PAIEMENT_CREE', l: 'Paiement créé' },
    { v: 'PAIEMENT_EN_RETARD', l: 'Paiement en retard' },
    { v: 'CHARGE_CREE', l: 'Charge créée' },
    { v: 'DOCUMENT_BIENTOT_EXPIRE', l: 'Document bientôt expiré' },
    { v: 'CANDIDAT_ADMIS', l: 'Candidat admis' },
    { v: 'ALERTE_VEHICULE', l: 'Alerte véhicule' },
    { v: 'UTILISATEUR_CREE', l: 'Utilisateur créé' },
    { v: 'MESSAGE_SYSTEME', l: 'Message système' },
  ];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  getCurrentUserId(): number {
    return this.authService.getCurrentUser()?.id ?? 0;
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.lister().subscribe({
      next: (data) => {
        this.notifications = this.appliquerFiltresRole(data);
        this.loading = false;
      },
      error: () => {
        this.notifications = this.appliquerFiltresRole(this.notificationsMock);
        this.loading = false;
      }
    });
  }

  private appliquerFiltresRole(liste: NotificationDTO[]): NotificationDTO[] {
    const userId = this.getCurrentUserId();
    if (userId > 0) {
      return liste.filter(n => n.destinataireId === userId || n.destinataireId === 1);
    }
    return liste;
  }

  get listeActive(): NotificationDTO[] {
    const list = this.notifications.length > 0 ? this.notifications : this.appliquerFiltresRole(this.notificationsMock);
    let result = list;

    if (this.filtreType) {
      result = result.filter(n => n.typeEvenement === this.filtreType);
    }

    if (this.filtreStatut === 'NON_LUES') {
      result = result.filter(n => !n.statutLecture);
    } else if (this.filtreStatut === 'LUES') {
      result = result.filter(n => n.statutLecture);
    }

    if (this.searchTerm) {
      const q = this.searchTerm.toLowerCase();
      result = result.filter(n => n.contenu.toLowerCase().includes(q));
    }

    return result.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  get nombreNonLues(): number {
    const list = this.notifications.length > 0 ? this.notifications : this.appliquerFiltresRole(this.notificationsMock);
    return list.filter(n => !n.statutLecture).length;
  }

  getTypeEvenementLabel(t: TypeEvenementNotification): string {
    return this.typesEvenements.find((x) => x.v === t)?.l ?? t;
  }

  getTypeEvenementClass(t: TypeEvenementNotification): string {
    const verts: TypeEvenementNotification[] = ['SEANCE_VALIDEE', 'CANDIDAT_ADMIS', 'PAIEMENT_CREE'];
    const rouges: TypeEvenementNotification[] = ['SEANCE_CONTESTEE', 'ALERTE_VEHICULE', 'PAIEMENT_EN_RETARD'];

    if (verts.includes(t)) return 'status-go';
    if (rouges.includes(t)) return 'status-stop';
    return 'status-warn';
  }

  getTypeEvenementIcon(t: TypeEvenementNotification): string {
    switch (t) {
      case 'NOUVELLE_SEANCE': return 'calendar-plus';
      case 'SEANCE_REPROGRAMMEE': return 'calendar-clock';
      case 'SEANCE_DECLAREE': return 'calendar-check';
      case 'SEANCE_VALIDEE': return 'check-circle';
      case 'SEANCE_CONTESTEE': return 'alert-circle';
      case 'SEANCE_A_VALIDER': return 'clock';
      case 'PAIEMENT_CREE': return 'dollar-sign';
      case 'PAIEMENT_EN_RETARD': return 'alert-triangle';
      case 'CHARGE_CREE': return 'file-text';
      case 'DOCUMENT_BIENTOT_EXPIRE': return 'file-warning';
      case 'CANDIDAT_ADMIS': return 'trophy';
      case 'ALERTE_VEHICULE': return 'car';
      case 'UTILISATEUR_CREE': return 'user-plus';
      case 'MESSAGE_SYSTEME': return 'info';
      default: return 'bell';
    }
  }

  getSvgIcon(icon: string): string {
    const icons: Record<string, string> = {
      'calendar-plus': '<path d="M16 2v4M8 2v4M3 10h18M13 14h2m-4 0h4m-7 0h.01M11 18h.01M15 18h.01"></path><rect x="3" y="4" width="18" height="18" rx="2"></rect>',
      'calendar-clock': '<path d="M16 2v4M8 2v4M3 10h18"></path><rect x="3" y="4" width="18" height="18" rx="2"></rect><circle cx="12" cy="16" r="3"></circle><path d="M12 14v2l1 1"></path>',
      'calendar-check': '<path d="M16 2v4M8 2v4M3 10h18"></path><rect x="3" y="4" width="18" height="18" rx="2"></rect><path d="M9 16l2 2 4-4"></path>',
      'check-circle': '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>',
      'alert-circle': '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>',
      'clock': '<circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline>',
      'dollar-sign': '<line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>',
      'alert-triangle': '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line>',
      'file-text': '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline>',
      'file-warning': '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="12" y1="12" x2="12" y2="16"></line><line x1="12" y1="20" x2="12.01" y2="20"></line>',
      'trophy': '<path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"></path><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"></path><path d="M4 22h16"></path><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"></path><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"></path><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"></path>',
      'car': '<path d="M14 16H9m10 0h3v-3.15a1 1 0 0 0-.84-.99L16 11l-2.7-3.6a1 1 0 0 0-.8-.4H5.24a2 2 0 0 0-1.8 1.1l-.8 1.63A6 6 0 0 0 2 12.42V16h2"></path><circle cx="6.5" cy="16.5" r="2.5"></circle><circle cx="16.5" cy="16.5" r="2.5"></circle>',
      'user-plus': '<path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line>',
      'info': '<circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line>',
      'bell': '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path>',
      'checks': '<polyline points="9 11 12 14 22 4"></polyline><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path>',
      'search': '<circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line>',
    };
    return icons[icon] ?? icons['bell'];
  }

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffH = Math.floor(diffMs / 3600000);
    const diffJ = Math.floor(diffMs / 86400000);

    if (diffMin < 1) return 'À l\'instant';
    if (diffMin < 60) return `Il y a ${diffMin} min`;
    if (diffH < 24) return `Il y a ${diffH}h`;
    if (diffJ < 7) return `Il y a ${diffJ}j`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  getStatutBadge(lu: boolean): { class: string; text: string } {
    if (lu) return { class: 'badge-gray', text: 'Lue' };
    return { class: 'badge-green', text: 'Non lue' };
  }

  marquerLue(id: number): void {
    const notif = this.listeActive.find(n => n.id === id);
    if (!notif || notif.statutLecture) return;

    this.notificationService.marquerLue(id).subscribe({
      next: (n) => {
        this.mettreAJourStatut(id, true);
      },
      error: () => {
        this.mettreAJourStatut(id, true);
      }
    });
  }

  toutMarquerLue(): void {
    this.notificationService.toutMarquerLue().subscribe({
      next: () => {
        this.marquerToutesLocalement();
      },
      error: () => {
        this.marquerToutesLocalement();
      }
    });
  }

  private mettreAJourStatut(id: number, lu: boolean): void {
    const n = this.notifications.find(x => x.id === id);
    if (n) n.statutLecture = lu;
    const m = this.notificationsMock.find(x => x.id === id);
    if (m) m.statutLecture = lu;
  }

  private marquerToutesLocalement(): void {
    const list = this.notifications.length > 0 ? this.notifications : this.notificationsMock;
    list.forEach(n => n.statutLecture = true);
  }

  getStatutLabel(s: string): string {
    return { TOUTES: 'Toutes', NON_LUES: 'Non lues', LUES: 'Lues' }[s as 'TOUTES' | 'NON_LUES' | 'LUES'] ?? s;
  }
}
