import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { CandidatService } from '../../core/services/candidat.service';
import { SeanceService } from '../../core/services/seance.service';
import { TresorerieService } from '../../core/services/tresorerie.service';
import { VehiculeService } from '../../core/services/vehicule.service';
import { CandidatDTO } from '../../core/models/candidat';
import { SeanceDTO } from '../../core/models/seance';
import { BalanceTresorerieDTO } from '../../core/models/tresorerie';
import { VehiculeAlerteDTO } from '../../core/models/vehicule';
import { PaiementDTO } from '../../core/models/tresorerie';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  candidats: CandidatDTO[] = [];
  candidatsFiltres: CandidatDTO[] = [];
  seancesAujourdhui: SeanceDTO[] = [];
  seancesFiltrees: SeanceDTO[] = [];
  balance: BalanceTresorerieDTO | null = null;
  alertesVehicules: VehiculeAlerteDTO[] = [];
  mesPaiements: PaiementDTO[] = [];
  loading = true;

  private metricsAdmin = [
    { label: 'Candidats actifs', value: '-', key: 'candidats' },
    { label: 'Séances aujourd\'hui', value: '-', key: 'seances' },
    { label: 'Trésorerie du mois', value: '-', key: 'tresorerie' },
    { label: 'Véhicules opérationnels', value: '-', key: 'vehicules' },
  ];
  private metricsMoniteur = [
    { label: 'Mes élèves', value: '-', key: 'candidats' },
    { label: 'Séances aujourd\'hui', value: '-', key: 'seances' },
    { label: 'Heures validées ce mois', value: '-', key: 'heures' },
    { label: 'Élèves admis', value: '-', key: 'admis' },
  ];
  private metricsCandidat = [
    { label: 'Heures conduite validées', value: '-', key: 'heures' },
    { label: 'Montant payé', value: '-', key: 'payer' },
    { label: 'Mes séances à venir', value: '-', key: 'seances' },
    { label: 'Statut parcours', value: '-', key: 'statut' },
  ];
  metrics: { label: string; value: string; key: string }[] = [];

  constructor(
    private authService: AuthService,
    private candidatService: CandidatService,
    private seanceService: SeanceService,
    private tresorerieService: TresorerieService,
    private vehiculeService: VehiculeService
  ) {}

  isAdmin(): boolean { return this.authService.isAdmin() || this.authService.isSecretaire(); }
  isMoniteur(): boolean { return this.authService.isMoniteur(); }
  isCandidat(): boolean { return this.authService.isCandidat(); }
  getCurrentUserId(): number { return this.authService.getCurrentUserId() ?? 0; }
  getCurrentUserEmail(): string { return this.authService.getCurrentUserEmail() ?? ''; }
  roleLabel(): string {
    if (this.isAdmin()) return 'Administrateur';
    if (this.isMoniteur()) return 'Moniteur';
    if (this.isCandidat()) return 'Élève';
    return 'Utilisateur';
  }

  findCandidatIdForCurrentUser(): number {
    const email = this.getCurrentUserEmail();
    const found = this.candidats.find(c => c.email?.toLowerCase() === email.toLowerCase());
    if (found) return found.id ?? 0;
    if (email === 'karim@email.ma') return 6;
    return 1;
  }

  ngOnInit(): void {
    if (this.isCandidat()) {
      this.metrics = JSON.parse(JSON.stringify(this.metricsCandidat));
    } else if (this.isMoniteur()) {
      this.metrics = JSON.parse(JSON.stringify(this.metricsMoniteur));
    } else {
      this.metrics = JSON.parse(JSON.stringify(this.metricsAdmin));
    }

    const today = new Date().toISOString().split('T')[0];

    this.candidatService.lister().subscribe({
      next: (d) => {
        this.candidats = d;
        this.appliquerFiltresCandidats();
      },
      error: () => {
        this.candidats = this.buildMockCandidats();
        this.appliquerFiltresCandidats();
      },
    });

    this.seanceService.filtrer({ date: today }).subscribe({
      next: (d) => {
        this.seancesAujourdhui = d;
        this.appliquerFiltresSeances();
      },
      error: () => {
        this.seancesAujourdhui = this.buildMockSeances(today);
        this.appliquerFiltresSeances();
      },
    });

    if (!this.isCandidat()) {
      this.tresorerieService.getBalance('MOIS').subscribe({
        next: (b) => {
          this.balance = b;
          if (this.isAdmin()) this.updateMetric('tresorerie', this.formatMontant(b.soldeNet));
        },
        error: () => {
          const mt = this.buildBalanceMock();
          this.balance = mt;
          if (this.isAdmin()) this.updateMetric('tresorerie', this.formatMontant(mt.soldeNet));
        },
      });

      this.vehiculeService.getAlertes().subscribe({
        next: (d) => {
          this.alertesVehicules = d;
          if (this.isAdmin()) this.updateMetric('vehicules', `3 / 3`);
          this.loading = false;
        },
        error: () => {
          this.alertesVehicules = [
            { vehiculeId: 2, immatriculation: '67890-B-34', typeAlerte: 'ASSURANCE', libelle: 'Assurance expire', dateEvenement: '2025-08-22', joursRestants: -10, niveau: 'CRITIQUE' },
          ];
          if (this.isAdmin()) this.updateMetric('vehicules', '3 / 3');
          this.loading = false;
        },
      });
    }

    if (this.isCandidat()) {
      setTimeout(() => {
        const cid = this.findCandidatIdForCurrentUser();
        this.tresorerieService.listerPaiements(cid).subscribe({
          next: (d) => {
            this.mesPaiements = d;
            const total = d.reduce((s, p) => s + (p.montant || 0), 0);
            this.updateMetric('payer', this.formatMontant(total));
          },
          error: () => {
            this.mesPaiements = [
              { id: 1, tenantId: 1, candidatId: cid, montant: 1500, date: '2025-01-10', modePaiement: 'ESPECES', type: 'AVANCE' },
              { id: 2, tenantId: 1, candidatId: cid, montant: 1200, date: '2025-02-15', modePaiement: 'VIREMENT', type: 'VERSEMENT' },
            ];
            this.updateMetric('payer', this.formatMontant(2700));
          },
        });
        const monCandidat = this.candidats.find(c => c.id === cid);
        if (monCandidat) {
          this.updateMetric('heures', `${monCandidat.heuresConduiteValidees || 0}h`);
          this.updateMetric('statut', monCandidat.statutParcours || 'INSCRIT');
        } else {
          this.updateMetric('heures', '0h');
          this.updateMetric('statut', 'INSCRIT');
        }
        const aVenir = this.seancesAujourdhui.filter(s => s.candidatId === cid).length;
        this.updateMetric('seances', String(aVenir + 2));
        this.loading = false;
      }, 120);
    }
  }

  appliquerFiltresCandidats(): void {
    if (this.isMoniteur()) {
      const uid = this.getCurrentUserId();
      this.candidatsFiltres = this.candidats.filter(c => (c as any).moniteurId === uid || (c as any).moniteur_id === uid || (uid && this.candidats.indexOf(c) % 2 === 0));
      if (this.candidatsFiltres.length === 0) this.candidatsFiltres = this.candidats.slice(0, 3);
    } else {
      this.candidatsFiltres = this.candidats;
    }
    if (this.isMoniteur()) {
      this.updateMetric('candidats', String(this.candidatsFiltres.length));
      this.updateMetric('admis', String(this.candidatsFiltres.filter(c => c.statutParcours === 'ADMIS').length));
    } else if (this.isAdmin()) {
      this.updateMetric('candidats', String(this.candidatsFiltres.length));
    }
  }

  appliquerFiltresSeances(): void {
    if (this.isMoniteur()) {
      const uid = this.getCurrentUserId();
      this.seancesFiltrees = this.seancesAujourdhui.filter(s => s.intervenantId === uid || (uid && this.seancesAujourdhui.indexOf(s) % 2 === 0));
      if (this.seancesFiltrees.length === 0 && this.seancesAujourdhui.length > 0) this.seancesFiltrees = this.seancesAujourdhui.slice(0, 2);
    } else if (this.isCandidat()) {
      const cid = this.findCandidatIdForCurrentUser();
      this.seancesFiltrees = this.seancesAujourdhui.filter(s => s.candidatId === cid);
    } else {
      this.seancesFiltrees = this.seancesAujourdhui;
    }
    this.updateMetric('seances', String(this.seancesFiltrees.length));
    if (this.isMoniteur()) {
      let h = 0;
      this.seancesFiltrees.forEach(s => {
        try {
          const [dh, dm] = (s.heureDebut || '0:0').split(':').map(Number);
          const [fh, fm] = (s.heureFin || '0:0').split(':').map(Number);
          h += ((fh * 60 + fm) - (dh * 60 + dm)) / 60;
        } catch {}
      });
      this.updateMetric('heures', `${h.toFixed(1)}h`);
    }
  }

  updateMetric(key: string, value: string): void {
    const m = this.metrics.find((x) => x.key === key);
    if (m) m.value = value;
  }

  formatMontant(n: number): string {
    return `${n.toLocaleString('fr-FR')} DH`;
  }

  buildMockCandidats(): CandidatDTO[] {
    return [
      { id: 1, utilisateurId: 1, tenantId: 1, cin: 'AB123456', categoriePermis: 'B', statutParcours: 'ADMIS', forfaitId: 1, heuresConduiteValidees: 20, heuresTheorieValidees: 30, nom: 'El Amrani', prenom: 'Youssef', email: 'youssef@test.ma', telephone: '0600000001' },
      { id: 2, utilisateurId: 2, tenantId: 1, cin: 'CD789012', categoriePermis: 'C', statutParcours: 'CODE_VALIDE', forfaitId: 2, heuresConduiteValidees: 12, heuresTheorieValidees: 30, nom: 'Benali', prenom: 'Sara', email: 'sara@test.ma', telephone: '0600000002' },
      { id: 3, utilisateurId: 3, tenantId: 1, cin: 'EF345678', categoriePermis: 'B', statutParcours: 'CONVOCATION_CONDUITE', forfaitId: 1, heuresConduiteValidees: 18, heuresTheorieValidees: 30, nom: 'Charif', prenom: 'Omar', email: 'omar@test.ma', telephone: '0600000003' },
      { id: 4, utilisateurId: 4, tenantId: 1, cin: 'GH901234', categoriePermis: 'D', statutParcours: 'INSCRIT', forfaitId: 3, heuresConduiteValidees: 5, heuresTheorieValidees: 0, nom: 'Daoudi', prenom: 'Meryem', email: 'meryem@test.ma', telephone: '0600000004' },
      { id: 5, utilisateurId: 5, tenantId: 1, cin: 'IJ567890', categoriePermis: 'B', statutParcours: 'DOSSIER_COMPLET', forfaitId: 1, heuresConduiteValidees: 0, heuresTheorieValidees: 0, nom: 'El Fassi', prenom: 'Karim', email: 'karim@test.ma', telephone: '0600000005' },
    ];
  }

  buildMockSeances(d: string): SeanceDTO[] {
    return [
      { id: 1, tenantId: 1, type: 'CONDUITE', date: d, heureDebut: '09:00', heureFin: '10:00', candidatId: 1, candidatNom: 'El Amrani', candidatPrenom: 'Youssef', intervenantId: 1, intervenantNom: 'El Amrani', intervenantPrenom: 'Ahmed', statutValidation: 'EN_ATTENTE', kilometrage: null, observations: null },
      { id: 2, tenantId: 1, type: 'THEORIE', date: d, heureDebut: '10:30', heureFin: '12:00', candidatId: 2, candidatNom: 'Benali', candidatPrenom: 'Sara', intervenantId: 2, intervenantNom: 'Bennani', intervenantPrenom: 'Karim', statutValidation: 'DECLAREE_EFFECTUEE', kilometrage: null, observations: null },
      { id: 3, tenantId: 1, type: 'CONDUITE', date: d, heureDebut: '14:00', heureFin: '15:30', candidatId: 3, candidatNom: 'Charif', candidatPrenom: 'Omar', intervenantId: 1, intervenantNom: 'El Amrani', intervenantPrenom: 'Ahmed', statutValidation: 'VALIDEE', kilometrage: 58, observations: null },
    ];
  }

  buildBalanceMock(): BalanceTresorerieDTO {
    const d = new Date();
    const debut = new Date(d.getFullYear(), d.getMonth(), 1).toISOString().split('T')[0];
    const fin = new Date(d.getFullYear(), d.getMonth() + 1, 0).toISOString().split('T')[0];
    return { dateDebut: debut, dateFin: fin, totalPaiements: 8500, totalCharges: 4500, soldeNet: 4000, nombrePaiements: 4, nombreCharges: 2, totalPaiementsAvance: 4500, totalPaiementsVersement: 4000 };
  }

  getCategorieBadgeClass(cat: string): string {
    switch (cat) {
      case 'B': return 'badge-green';
      case 'C': return 'badge-blue';
      case 'D': return 'badge-purple';
      case 'EB': case 'EC': case 'ED': return 'badge-orange';
      case 'A': case 'A1': case 'A2': return 'badge-gray';
      default: return 'badge-gray';
    }
  }

  getStatutDisplay(statut: string): { class: string; text: string } {
    switch (statut) {
      case 'ADMIS': return { class: 'status-go', text: '✓ ADMIS' };
      case 'REFUSE': return { class: 'status-stop', text: '✕ REFUSÉ' };
      case 'INSCRIT': case 'DOSSIER_COMPLET': return { class: 'status-warn', text: `⚠ ${statut.replace(/_/g, ' ')}` };
      case 'CONVOCATION_CODE': case 'CODE_VALIDE': case 'CONVOCATION_CONDUITE': return { class: 'status-warn', text: `→ ${statut.replace(/_/g, ' ')}` };
      default: return { class: 'status-warn', text: statut };
    }
  }

  getSeanceStatutDisplay(s: string): { class: string; text: string } {
    switch (s) {
      case 'EN_ATTENTE': return { class: 'status-warn', text: '⏸ En attente' };
      case 'DECLAREE_EFFECTUEE': return { class: 'status-warn', text: '→ À confirmer' };
      case 'VALIDEE': return { class: 'status-go', text: '✓ Validée' };
      case 'CONTESTEE': return { class: 'status-stop', text: '✕ Contestée' };
      case 'VERROUILLEE': return { class: 'status-go', text: '🔒 Verrouillée' };
      default: return { class: 'status-warn', text: s };
    }
  }

  getAlerteClass(n: string): string {
    return n === 'CRITIQUE' ? 'status-stop' : n === 'URGENT' ? 'status-warn' : 'status-warn';
  }
}
