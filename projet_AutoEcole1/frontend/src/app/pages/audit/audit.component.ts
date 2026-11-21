import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';
import { AuditLog, ActionAudit } from '../../core/models/audit';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './audit.component.html',
})
export class AuditComponent implements OnInit {
  logs: AuditLog[] = [];
  loading = false;

  filtreDateDebut: string = '';
  filtreDateFin: string = '';
  filtreAction: ActionAudit | '' = '';
  filtreUtilisateurId: number | null = null;

  page = 1;
  size = 10;
  totalPages = 3;

  actionsAudit: { v: ActionAudit; l: string }[] = [
    { v: 'LOGIN', l: 'Connexion' },
    { v: 'LOGOUT', l: 'Déconnexion' },
    { v: 'CREATE_CANDIDAT', l: 'Créer candidat' },
    { v: 'UPDATE_CANDIDAT', l: 'Modifier candidat' },
    { v: 'DELETE_CANDIDAT', l: 'Supprimer candidat' },
    { v: 'CREATE_PAIEMENT', l: 'Créer paiement' },
    { v: 'UPDATE_PAIEMENT', l: 'Modifier paiement' },
    { v: 'DELETE_PAIEMENT', l: 'Supprimer paiement' },
    { v: 'CREATE_CHARGE', l: 'Créer charge' },
    { v: 'UPDATE_CHARGE', l: 'Modifier charge' },
    { v: 'DELETE_CHARGE', l: 'Supprimer charge' },
    { v: 'CREATE_SEANCE', l: 'Créer séance' },
    { v: 'UPDATE_SEANCE', l: 'Modifier séance' },
    { v: 'DELETE_SEANCE', l: 'Supprimer séance' },
    { v: 'VALIDER_SEANCE', l: 'Valider séance' },
    { v: 'CONTESTER_SEANCE', l: 'Contester séance' },
    { v: 'UPLOAD_DOCUMENT', l: 'Upload document' },
    { v: 'VALIDER_DOCUMENT', l: 'Valider document' },
    { v: 'REFUSER_DOCUMENT', l: 'Refuser document' },
    { v: 'CREER_UTILISATEUR', l: 'Créer utilisateur' },
    { v: 'MODIFIER_UTILISATEUR', l: 'Modifier utilisateur' },
    { v: 'DESACTIVER_UTILISATEUR', l: 'Désactiver utilisateur' },
  ];

  logsMock: AuditLog[] = [
    { id: 1, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'LOGIN', description: 'Connexion réussie depuis tableau de bord', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 3600000 * 2).toISOString() },
    { id: 2, tenantId: 1, utilisateurId: 2, utilisateurNom: 'Ahmed Moniteur', action: 'CREATE_CANDIDAT', description: 'Création du candidat: Karim El Fassi', adresseIp: '192.168.1.15', userAgent: 'Firefox 121 / macOS', dateCreation: new Date(Date.now() - 3600000 * 5).toISOString() },
    { id: 3, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'UPDATE_PAIEMENT', description: 'Modification du paiement #3: montant 1500 → 1800 DH', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 3600000 * 8).toISOString() },
    { id: 4, tenantId: 1, utilisateurId: 5, utilisateurNom: 'Fatima Gharbi', action: 'VALIDER_SEANCE', description: 'Validation séance conduite #12 par moniteur', adresseIp: '10.0.0.5', userAgent: 'Safari 17 / iOS 17', dateCreation: new Date(Date.now() - 86400000).toISOString() },
    { id: 5, tenantId: 1, utilisateurId: 3, utilisateurNom: 'Sara Benali', action: 'CONTESTER_SEANCE', description: 'Contestation séance #8: heure non effectuée', adresseIp: '172.16.0.8', userAgent: 'Chrome Mobile 120 / Android 13', dateCreation: new Date(Date.now() - 86400000 * 2).toISOString() },
    { id: 6, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'DELETE_CHARGE', description: 'Suppression charge: CARBURANT #4 (600 DH)', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 86400000 * 2).toISOString() },
    { id: 7, tenantId: 1, utilisateurId: 2, utilisateurNom: 'Ahmed Moniteur', action: 'UPLOAD_DOCUMENT', description: 'Upload CIN pour candidat #5', adresseIp: '192.168.1.15', userAgent: 'Firefox 121 / macOS', dateCreation: new Date(Date.now() - 86400000 * 3).toISOString() },
    { id: 8, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'VALIDER_DOCUMENT', description: 'Document CIN candidat #5 validé', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 86400000 * 3).toISOString() },
    { id: 9, tenantId: 1, utilisateurId: 4, utilisateurNom: 'Omar Charif', action: 'LOGOUT', description: 'Déconnexion session #4821', adresseIp: '192.168.1.22', userAgent: 'Edge 120 / Windows 10', dateCreation: new Date(Date.now() - 86400000 * 4).toISOString() },
    { id: 10, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'DESACTIVER_UTILISATEUR', description: 'Désactivation compte utilisateur #7: Salma Idrissi', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 86400000 * 5).toISOString() },
    { id: 11, tenantId: 1, utilisateurId: 6, utilisateurNom: 'Meryem Daoudi', action: 'REFUSER_DOCUMENT', description: 'Refus permis provisoire candidat #2: photo illisible', adresseIp: '10.0.0.12', userAgent: 'Safari 17 / iOS 16', dateCreation: new Date(Date.now() - 86400000 * 6).toISOString() },
    { id: 12, tenantId: 1, utilisateurId: 1, utilisateurNom: 'Admin AutoEcole', action: 'CREER_UTILISATEUR', description: 'Création utilisateur: Nadia El Amrani (role: SECRETAIRE)', adresseIp: '192.168.1.10', userAgent: 'Chrome 120 / Windows 11', dateCreation: new Date(Date.now() - 86400000 * 7).toISOString() },
  ];

  constructor(
    private auditService: AuditService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.chargerLogs();
  }

  chargerLogs(): void {
    this.loading = true;
    const params: any = { page: this.page, size: this.size };
    if (this.filtreAction) params.action = this.filtreAction;
    if (this.filtreUtilisateurId) params.utilisateurId = this.filtreUtilisateurId;
    if (this.filtreDateDebut) params.dateDebut = this.filtreDateDebut;
    if (this.filtreDateFin) params.dateFin = this.filtreDateFin;

    this.auditService.lister(params).subscribe({
      next: (data) => {
        this.logs = data;
        this.loading = false;
      },
      error: () => {
        this.logs = this.appliquerFiltres(this.logsMock);
        this.totalPages = Math.max(1, Math.ceil(this.logs.length / this.size));
        this.loading = false;
      }
    });
  }

  filtrer(): void {
    this.page = 1;
    this.chargerLogs();
  }

  reinitialiser(): void {
    this.filtreDateDebut = '';
    this.filtreDateFin = '';
    this.filtreAction = '';
    this.filtreUtilisateurId = null;
    this.page = 1;
    this.chargerLogs();
  }

  private appliquerFiltres(liste: AuditLog[]): AuditLog[] {
    let result = [...liste];
    if (this.filtreAction) result = result.filter(l => l.action === this.filtreAction);
    if (this.filtreUtilisateurId) result = result.filter(l => l.utilisateurId === this.filtreUtilisateurId);
    if (this.filtreDateDebut) {
      const dd = new Date(this.filtreDateDebut).getTime();
      result = result.filter(l => new Date(l.dateCreation).getTime() >= dd);
    }
    if (this.filtreDateFin) {
      const df = new Date(this.filtreDateFin + 'T23:59:59').getTime();
      result = result.filter(l => new Date(l.dateCreation).getTime() <= df);
    }
    return result;
  }

  getLogsFiltres(): AuditLog[] {
    if (this.logs.length > 0) return this.logs;
    const tous = this.appliquerFiltres(this.logsMock);
    this.totalPages = Math.max(1, Math.ceil(tous.length / this.size));
    const debut = (this.page - 1) * this.size;
    return tous.slice(debut, debut + this.size);
  }

  getActionBadgeClass(action: ActionAudit): string {
    if (action === 'LOGIN' || action.startsWith('CREATE_') || action.startsWith('VALIDER_')) {
      return 'badge-green';
    }
    if (action.startsWith('UPDATE_') || action.startsWith('MODIFIER_')) {
      return 'badge-blue';
    }
    if (action.startsWith('DELETE_') || action.startsWith('DESACTIVER_')) {
      return 'badge-orange';
    }
    if (action.startsWith('CONTESTER_') || action.startsWith('REFUSER_')) {
      return 'badge-red';
    }
    if (action === 'LOGOUT') {
      return 'badge-gray';
    }
    return 'badge-gray';
  }

  getActionLabel(action: ActionAudit): string {
    return this.actionsAudit.find(a => a.v === action)?.l ?? action;
  }

  formatDateHeure(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  pagePrecedente(): void {
    if (this.page > 1) {
      this.page--;
      this.chargerLogs();
    }
  }

  pageSuivante(): void {
    if (this.page < this.totalPages) {
      this.page++;
      this.chargerLogs();
    }
  }

  getTotalPages(): number {
    if (this.logs.length > 0) return this.totalPages;
    const tous = this.appliquerFiltres(this.logsMock);
    return Math.max(1, Math.ceil(tous.length / this.size));
  }
}
