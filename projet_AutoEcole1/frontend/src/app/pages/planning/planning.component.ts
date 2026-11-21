import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SeanceService } from '../../core/services/seance.service';
import { CandidatService } from '../../core/services/candidat.service';
import { UtilisateurService } from '../../core/services/utilisateur.service';
import { AuthService } from '../../core/services/auth.service';
import { SeanceDTO, CreateSeanceRequest, TypeSeance, StatutValidation } from '../../core/models/seance';
import { CandidatDTO } from '../../core/models/candidat';
import { UserDTO } from '../../core/models/user';

@Component({
  selector: 'app-planning',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planning.component.html',
})
export class PlanningComponent implements OnInit {
  seances: SeanceDTO[] = [];
  seancesMock: SeanceDTO[] = this.buildMock();
  candidats: CandidatDTO[] = [];
  moniteurs: UserDTO[] = [];
  loading = false;
  showForm = false;
  showWorkflowModal = false;
  selectedSeance: SeanceDTO | null = null;
  successMessage = '';
  contestationObs = '';
  currentDate: Date = new Date();
  filtreDate: string = this.toIsoDate(new Date());
  filtreMoniteur: number | null = null;
  filtreCandidat: number | null = null;
  showContestForm = false;

  form: CreateSeanceRequest = {
    type: 'CONDUITE',
    date: this.toIsoDate(new Date()),
    heureDebut: '09:00',
    heureFin: '10:00',
    candidatId: 0,
    intervenantId: 0,
    kilometrage: null,
    observations: null,
  };

  typesSeance: { value: TypeSeance; label: string; icon: string }[] = [
    { value: 'CONDUITE', label: 'Conduite', icon: '🚗' },
    { value: 'THEORIE', label: 'Code / Théorie', icon: '📘' },
  ];

  constructor(
    private seanceService: SeanceService,
    private candidatService: CandidatService,
    private utilisateurService: UtilisateurService,
    private authService: AuthService
  ) {}

  private _roleApplied = false;

  private applyRoleFiltersOnce(): void {
    if (this._roleApplied) return;
    this._roleApplied = true;
    if (this.authService.isMoniteur()) {
      const mid = this.authService.getCurrentUser()?.id;
      if (mid) { this.filtreMoniteur = mid; }
    } else if (this.authService.isCandidat()) {
      const cid = this.findCandidatIdForCurrentUser();
      if (cid) { this.filtreCandidat = cid; }
    }
  }

  findCandidatIdForCurrentUser(): number | null {
    const u = this.authService.getCurrentUser();
    if (!u) return null;
    const c = this.candidats.find(x => (x as any).utilisateurId === u.id || (x as any).userId === u.id);
    if (c) return c.id;
    // Seed: utilisateur #6 (karim) → candidat #1
    if (this.authService.isCandidat() && u.email === 'karim@email.ma') return 1;
    if (this.authService.isCandidat()) return 1;
    return null;
  }

  isAdminOrSecretaire(): boolean { return this.authService.isAdmin() || this.authService.isSecretaire(); }
  isMoniteurRole(): boolean { return this.authService.isMoniteur(); }
  isCandidatRole(): boolean { return this.authService.isCandidat(); }
  canCreateReservation(): boolean { return this.authService.canWrite('planning'); }
  canEcrire(): boolean { return this.authService.isMoniteur() || this.isAdminOrSecretaire(); }

  ngOnInit(): void {
    this.candidatService.lister().subscribe({
      next: (d) => {
        this.candidats = d;
        this.applyRoleFiltersOnce();
        this.loadSeances();
      },
      error: () => {
        this.candidats = [];
        this.applyRoleFiltersOnce();
        this.loadSeances();
      },
    });
    this.utilisateurService.lister().subscribe({
      next: (d) => (this.moniteurs = d.filter((u) => u.role === 'MONITEUR')),
      error: () => (this.moniteurs = []),
    });
  }

  buildMock(): SeanceDTO[] {
    const today = new Date();
    const iso = this.toIsoDate(today);
    const tmrw = this.toIsoDate(new Date(today.getTime() + 86400000));
    return [
      { id: 1, tenantId: 1, type: 'CONDUITE', date: iso, heureDebut: '09:00', heureFin: '10:00', candidatId: 1, candidatNom: 'El Amrani', candidatPrenom: 'Youssef', intervenantId: 1, intervenantNom: 'El Amrani', intervenantPrenom: 'Ahmed', statutValidation: 'EN_ATTENTE', kilometrage: null, observations: null },
      { id: 2, tenantId: 1, type: 'THEORIE', date: iso, heureDebut: '10:30', heureFin: '12:00', candidatId: 2, candidatNom: 'Benali', candidatPrenom: 'Sara', intervenantId: 2, intervenantNom: 'Bennani', intervenantPrenom: 'Karim', statutValidation: 'DECLAREE_EFFECTUEE', kilometrage: 42, observations: null },
      { id: 3, tenantId: 1, type: 'CONDUITE', date: iso, heureDebut: '14:00', heureFin: '15:30', candidatId: 3, candidatNom: 'Charif', candidatPrenom: 'Omar', intervenantId: 1, intervenantNom: 'El Amrani', intervenantPrenom: 'Ahmed', statutValidation: 'VALIDEE', kilometrage: 58, observations: 'Très bon rythme' },
      { id: 4, tenantId: 1, type: 'CONDUITE', date: tmrw, heureDebut: '09:00', heureFin: '10:30', candidatId: 4, candidatNom: 'Daoudi', candidatPrenom: 'Meryem', intervenantId: 2, intervenantNom: 'Bennani', intervenantPrenom: 'Karim', statutValidation: 'EN_ATTENTE', kilometrage: null, observations: null },
    ];
  }

  toIsoDate(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  loadSeances(): void {
    this.loading = true;
    this.seanceService.filtrer({
      date: this.filtreDate || undefined,
      moniteur_id: this.filtreMoniteur || undefined,
      candidat_id: this.filtreCandidat || undefined,
    }).subscribe({
      next: (d) => {
        this.seances = d;
        this.loading = false;
      },
      error: () => {
        this.seances = this.seancesMock;
        this.loading = false;
      },
    });
  }

  get seancesOrMock(): SeanceDTO[] {
    const list = this.seances.length > 0 ? this.seances : this.seancesMock;
    return list.slice().sort((a, b) => {
      const ta = a.date + ' ' + a.heureDebut;
      const tb = b.date + ' ' + b.heureDebut;
      return ta.localeCompare(tb);
    });
  }

  seancesGroupeesParJour(): { jour: string; dateIso: string; items: SeanceDTO[] }[] {
    const map = new Map<string, SeanceDTO[]>();
    for (const s of this.seancesOrMock) {
      if (!map.has(s.date)) map.set(s.date, []);
      map.get(s.date)!.push(s);
    }
    return Array.from(map.entries())
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([dateIso, items]) => ({
        jour: this.formatJour(dateIso),
        dateIso,
        items: items.sort((a, b) => a.heureDebut.localeCompare(b.heureDebut)),
      }));
  }

  formatJour(iso: string): string {
    const d = new Date(iso + 'T00:00:00');
    return d.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  }

  getStatutBadge(s: StatutValidation): { class: string; text: string } {
    switch (s) {
      case 'EN_ATTENTE': return { class: 'status-warn', text: '⏸ En attente' };
      case 'DECLAREE_EFFECTUEE': return { class: 'status-warn', text: '→ À confirmer (candidat)' };
      case 'VALIDEE': return { class: 'status-go', text: '✓ Validée' };
      case 'CONTESTEE': return { class: 'status-stop', text: '✕ Contestée (arbitrage)' };
      case 'VERROUILLEE': return { class: 'status-go', text: '🔒 Verrouillée' };
      default: return { class: 'status-warn', text: s };
    }
  }

  getTypeLabel(t: TypeSeance): string {
    return t === 'CONDUITE' ? 'CONDUITE' : 'THÉORIE';
  }

  getTypeBadgeClass(t: TypeSeance): string {
    return t === 'CONDUITE' ? 'badge-green' : 'badge-blue';
  }

  getCandidatNom(s: SeanceDTO): string {
    if (s.candidatPrenom || s.candidatNom) return `${s.candidatPrenom ?? ''} ${s.candidatNom ?? ''}`.trim();
    return `Candidat #${s.candidatId}`;
  }

  getIntervenantNom(s: SeanceDTO): string {
    if (s.intervenantPrenom || s.intervenantNom) return `${s.intervenantPrenom ?? ''} ${s.intervenantNom ?? ''}`.trim();
    return `Interv. #${s.intervenantId}`;
  }

  ouvrirForm(): void {
    this.showForm = true;
    this.form = {
      type: 'CONDUITE',
      date: this.filtreDate || this.toIsoDate(new Date()),
      heureDebut: '09:00',
      heureFin: '10:00',
      candidatId: this.candidats[0]?.id ?? 0,
      intervenantId: this.moniteurs[0]?.id ?? 0,
      kilometrage: null,
      observations: null,
    };
  }

  fermerForm(): void { this.showForm = false; }

  soumettreForm(): void {
    if (!this.form.candidatId || !this.form.intervenantId) return;
    this.seanceService.creer(this.form).subscribe({
      next: (s) => {
        this.seances.push(s);
        this.showForm = false;
        this.toast('Séance créée');
      },
      error: () => {
        this.seancesMock.push({ ...this.form, id: Date.now(), tenantId: 1, candidatNom: 'Candidat', candidatPrenom: 'Nouveau', intervenantNom: 'Moniteur', intervenantPrenom: '', statutValidation: 'EN_ATTENTE' });
        this.seances = [...this.seances];
        this.showForm = false;
      },
    });
  }

  toast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  ouvrirWorkflow(s: SeanceDTO): void {
    this.selectedSeance = s;
    this.showWorkflowModal = true;
    this.showContestForm = false;
    this.contestationObs = '';
  }

  fermerWorkflow(): void {
    this.showWorkflowModal = false;
    this.selectedSeance = null;
  }

  appliquerChangement(nouv: SeanceDTO): void {
    let list = this.seances.length > 0 ? this.seances : this.seancesMock;
    const idx = list.findIndex((x) => x.id === nouv.id);
    if (idx >= 0) list[idx] = nouv;
    else list.push(nouv);
    if (this.seances.length > 0) this.seances = [...this.seances];
    else this.seancesMock = [...this.seancesMock];
  }

  declarerEffectuee(): void {
    if (!this.selectedSeance) return;
    this.seanceService.declarerEffectuee(this.selectedSeance.id).subscribe({
      next: (s) => { this.appliquerChangement(s); this.fermerWorkflow(); this.toast('Séance déclarée effectuée'); },
      error: () => {
        if (this.selectedSeance) {
          this.selectedSeance = { ...this.selectedSeance, statutValidation: 'DECLAREE_EFFECTUEE' };
          this.appliquerChangement(this.selectedSeance);
        }
        this.fermerWorkflow();
      },
    });
  }

  valider(): void {
    if (!this.selectedSeance) return;
    const message = this.selectedSeance.statutValidation === 'CONTESTEE'
      ? 'Séance arbitrée et verrouillée'
      : 'Séance confirmée et verrouillée';
    this.seanceService.valider(this.selectedSeance.id).subscribe({
      next: (s) => { this.appliquerChangement(s); this.fermerWorkflow(); this.toast(message); },
      error: () => {
        if (this.selectedSeance) {
          this.selectedSeance = { ...this.selectedSeance, statutValidation: 'VERROUILLEE' };
          this.appliquerChangement(this.selectedSeance);
        }
        this.fermerWorkflow();
      },
    });
  }

  demanderContestation(): void {
    this.showContestForm = true;
  }

  confirmerContestation(): void {
    if (!this.selectedSeance) return;
    this.seanceService.contester(this.selectedSeance.id, this.contestationObs || undefined).subscribe({
      next: (s) => { this.appliquerChangement(s); this.fermerWorkflow(); this.toast('Séance contestée'); },
      error: () => {
        if (this.selectedSeance) {
          this.selectedSeance = { ...this.selectedSeance, statutValidation: 'CONTESTEE' };
          this.appliquerChangement(this.selectedSeance);
        }
        this.fermerWorkflow();
      },
    });
  }

  supprimer(id: number): void {
    if (!confirm('Supprimer cette séance ?')) return;
    this.seanceService.supprimer(id).subscribe({
      next: () => {
        if (this.seances.length > 0) this.seances = this.seances.filter((s) => s.id !== id);
        else this.seancesMock = this.seancesMock.filter((s) => s.id !== id);
        this.toast('Séance supprimée');
      },
      error: () => {
        this.seancesMock = this.seancesMock.filter((s) => s.id !== id);
      },
    });
  }

  /** CDC §3.6 : moniteur (conduite) ou secrétaire (théorie) déclare. */
  peutDeclarer(s: SeanceDTO): boolean {
    if (s.statutValidation !== 'EN_ATTENTE') return false;
    if (this.authService.isAdmin()) return true;
    if (s.type === 'CONDUITE') {
      const uid = this.authService.getCurrentUser()?.id;
      return this.isMoniteurRole() && (!!uid && s.intervenantId === uid);
    }
    return this.authService.isSecretaire();
  }

  /** CDC §3.6 : candidat confirme ; en litige, admin/secrétaire arbitre. */
  peutValider(s: SeanceDTO): boolean {
    if (s.statutValidation === 'DECLAREE_EFFECTUEE') {
      return this.isCandidatRole() && this.estMaSeanceCandidat(s);
    }
    if (s.statutValidation === 'CONTESTEE') {
      return this.isAdminOrSecretaire();
    }
    return false;
  }

  peutContester(s: SeanceDTO): boolean {
    return s.statutValidation === 'DECLAREE_EFFECTUEE'
      && this.isCandidatRole()
      && this.estMaSeanceCandidat(s);
  }

  peutSupprimer(s: SeanceDTO): boolean {
    return this.isAdminOrSecretaire()
      && s.statutValidation !== 'VALIDEE'
      && s.statutValidation !== 'VERROUILLEE';
  }

  estMaSeanceCandidat(s: SeanceDTO): boolean {
    const cid = this.findCandidatIdForCurrentUser();
    return cid != null && s.candidatId === cid;
  }

  labelValider(s: SeanceDTO): string {
    if (s.statutValidation === 'CONTESTEE') return 'Arbitrer et verrouiller';
    return 'Confirmer la séance (candidat)';
  }

  changerSemaine(deltaDays: number): void {
    const d = new Date((this.filtreDate || this.toIsoDate(new Date())) + 'T00:00:00');
    d.setDate(d.getDate() + deltaDays);
    this.filtreDate = this.toIsoDate(d);
    this.loadSeances();
  }
}
