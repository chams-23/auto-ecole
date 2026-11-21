import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TresorerieService } from '../../core/services/tresorerie.service';
import { CandidatService } from '../../core/services/candidat.service';
import { AuthService } from '../../core/services/auth.service';
import {
  PaiementDTO,
  ChargeDTO,
  BalanceTresorerieDTO,
  CreatePaiementRequest,
  CreateChargeRequest,
  ModePaiement,
  TypePaiement,
  CategorieCharge,
  PeriodiciteCharge,
} from '../../core/models/tresorerie';
import { CandidatDTO } from '../../core/models/candidat';

@Component({
  selector: 'app-tresorerie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tresorerie.component.html',
})
export class TresorerieComponent implements OnInit {
  balance: BalanceTresorerieDTO | null = null;
  paiements: PaiementDTO[] = [];
  charges: ChargeDTO[] = [];
  candidats: CandidatDTO[] = [];

  paiementsMock: PaiementDTO[] = [
    { id: 1, tenantId: 1, candidatId: 1, candidatNom: 'El Amrani', candidatPrenom: 'Youssef', montant: 3000, modePaiement: 'ESPECES', date: new Date().toISOString().split('T')[0], type: 'AVANCE', recuUrl: null },
    { id: 2, tenantId: 1, candidatId: 2, candidatNom: 'Benali', candidatPrenom: 'Sara', montant: 2000, modePaiement: 'VIREMENT', date: new Date(Date.now() - 86400000 * 3).toISOString().split('T')[0], type: 'VERSEMENT', recuUrl: null },
    { id: 3, tenantId: 1, candidatId: 3, candidatNom: 'Charif', candidatPrenom: 'Omar', montant: 1500, modePaiement: 'CHEQUE', date: new Date(Date.now() - 86400000 * 7).toISOString().split('T')[0], type: 'AVANCE', recuUrl: null },
  ];

  chargesMock: ChargeDTO[] = [
    { id: 1, tenantId: 1, categorie: 'LOYER', montant: 4500, date: new Date(Date.now() - 86400000 * 2).toISOString().split('T')[0], periodicite: 'MENSUELLE', justificatifUrl: null },
    { id: 2, tenantId: 1, categorie: 'ASSURANCE', montant: 1200, date: new Date(Date.now() - 86400000 * 10).toISOString().split('T')[0], periodicite: 'ANNUELLE', justificatifUrl: null },
    { id: 3, tenantId: 1, categorie: 'CARBURANT', montant: 600, date: new Date(Date.now() - 86400000 * 4).toISOString().split('T')[0], periodicite: 'PONCTUELLE', justificatifUrl: null },
  ];

  loading = false;
  periode: 'SEMAINE' | 'MOIS' | 'TRIMESTRE' | 'ANNEE' = 'MOIS';
  ongletActif: 'PAIEMENTS' | 'CHARGES' = 'PAIEMENTS';
  showPaiementForm = false;
  showChargeForm = false;
  successMessage = '';

  formPaiement: CreatePaiementRequest = {
    candidatId: 0,
    montant: 0,
    modePaiement: 'ESPECES',
    date: new Date().toISOString().split('T')[0],
    type: 'VERSEMENT',
    recuUrl: null,
  };

  formCharge: CreateChargeRequest = {
    categorie: 'AUTRE',
    montant: 0,
    date: new Date().toISOString().split('T')[0],
    periodicite: 'PONCTUELLE',
    justificatifUrl: null,
  };

  modesPaiement: { v: ModePaiement; l: string }[] = [
    { v: 'ESPECES', l: 'Espèces' },
    { v: 'CHEQUE', l: 'Chèque' },
    { v: 'VIREMENT', l: 'Virement' },
    { v: 'CARTE_BANCAIRE', l: 'Carte bancaire' },
  ];

  typesPaiement: { v: TypePaiement; l: string }[] = [
    { v: 'AVANCE', l: 'Acompte / Avance' },
    { v: 'VERSEMENT', l: 'Versement' },
  ];

  categoriesCharge: { v: CategorieCharge; l: string }[] = [
    { v: 'LOYER', l: 'Loyer' },
    { v: 'ASSURANCE', l: 'Assurance' },
    { v: 'CARBURANT', l: 'Carburant' },
    { v: 'ENTRETIEN', l: 'Entretien' },
    { v: 'SALAIRE', l: 'Salaire' },
    { v: 'FORMATIONS', l: 'Formations' },
    { v: 'FOURNITURES', l: 'Fournitures' },
    { v: 'AUTRE', l: 'Autre' },
  ];

  periodicites: { v: PeriodiciteCharge; l: string }[] = [
    { v: 'PONCTUELLE', l: 'Ponctuelle' },
    { v: 'MENSUELLE', l: 'Mensuelle' },
    { v: 'TRIMESTRIELLE', l: 'Trimestrielle' },
    { v: 'ANNUELLE', l: 'Annuelle' },
  ];

  constructor(
    private tresorerieService: TresorerieService,
    private candidatService: CandidatService,
    private authService: AuthService
  ) {}

  canWriteTresorerie(): boolean { return this.authService.canWrite('tresorerie'); }
  isCandidatRole(): boolean { return this.authService.isCandidat(); }
  showChargesTab(): boolean { return !this.isCandidatRole(); }

  getCurrentCandidatId(): number | null {
    const u = this.authService.getCurrentUser();
    if (!u) return null;
    if (this.authService.isCandidat() && u.email === 'karim@email.ma') return 6;
    if (this.authService.isCandidat()) return 1;
    return null;
  }

  ngOnInit(): void {
    this.loadAll();
    this.candidatService.lister().subscribe({
      next: (d) => (this.candidats = d),
      error: () => (this.candidats = []),
    });
  }

  loadAll(): void {
    this.loading = true;
    const cid = this.isCandidatRole() ? this.getCurrentCandidatId() : null;
    this.tresorerieService.getBalance(this.periode).subscribe({
      next: (b) => { this.balance = this.isCandidatRole() ? this.buildBalanceMock() : b; this.loading = false; },
      error: () => { this.balance = this.buildBalanceMock(); this.loading = false; },
    });
    this.tresorerieService.listerPaiements(cid ?? undefined).subscribe({
      next: (d) => (this.paiements = this.filterCandidatPaiements(d)),
      error: () => (this.paiements = this.filterCandidatPaiements(this.paiementsMock)),
    });
    if (!this.isCandidatRole()) {
      this.tresorerieService.listerCharges().subscribe({
        next: (d) => (this.charges = d),
        error: () => (this.charges = this.chargesMock),
      });
    }
  }

  private filterCandidatPaiements(list: PaiementDTO[]): PaiementDTO[] {
    const cid = this.getCurrentCandidatId();
    if (!this.isCandidatRole() || !cid) return list;
    return list.filter(p => p.candidatId === cid);
  }

  buildBalanceMock(): BalanceTresorerieDTO {
    const p = this.paiementsMock;
    const c = this.chargesMock;
    const totalP = p.reduce((s, x) => s + x.montant, 0);
    const totalC = c.reduce((s, x) => s + x.montant, 0);
    const avance = p.filter((x) => x.type === 'AVANCE').reduce((s, x) => s + x.montant, 0);
    const vers = p.filter((x) => x.type === 'VERSEMENT').reduce((s, x) => s + x.montant, 0);
    return {
      dateDebut: new Date(Date.now() - 86400000 * 15).toISOString().split('T')[0],
      dateFin: new Date().toISOString().split('T')[0],
      totalPaiements: totalP,
      totalCharges: totalC,
      soldeNet: totalP - totalC,
      nombrePaiements: p.length,
      nombreCharges: c.length,
      totalPaiementsAvance: avance,
      totalPaiementsVersement: vers,
    };
  }

  getMontantDH(n?: number | null): string {
    return `${(n ?? 0).toLocaleString('fr-FR')} DH`;
  }

  getSoldeClass(v: number): string {
    if (v > 0) return 'text-go';
    if (v < 0) return 'text-stop';
    return 'text-secondary';
  }

  getPeriodeTexte(): string {
    return { SEMAINE: 'la semaine', MOIS: 'le mois', TRIMESTRE: 'le trimestre', ANNEE: "l'année" }[this.periode];
  }

  formatDate(d: string): string {
    return new Date(d + 'T00:00:00').toLocaleDateString('fr-FR');
  }

  getModePaiementLabel(m: ModePaiement): string {
    return this.modesPaiement.find((x) => x.v === m)?.l ?? m;
  }

  getCategorieChargeLabel(c: CategorieCharge): string {
    return this.categoriesCharge.find((x) => x.v === c)?.l ?? c;
  }

  getCategorieChargeBadgeClass(c: CategorieCharge): string {
    switch (c) {
      case 'LOYER': return 'badge-purple';
      case 'ASSURANCE': return 'badge-blue';
      case 'CARBURANT': return 'badge-orange';
      case 'ENTRETIEN': return 'badge-green';
      case 'SALAIRE': return 'badge-blue';
      default: return 'badge-gray';
    }
  }

  ouvrirPaiement(): void {
    this.showPaiementForm = true;
    this.formPaiement = {
      candidatId: this.candidats[0]?.id ?? 0,
      montant: 0,
      modePaiement: 'ESPECES',
      date: new Date().toISOString().split('T')[0],
      type: 'VERSEMENT',
      recuUrl: null,
    };
  }

  ouvrirCharge(): void {
    this.showChargeForm = true;
    this.formCharge = {
      categorie: 'AUTRE',
      montant: 0,
      date: new Date().toISOString().split('T')[0],
      periodicite: 'PONCTUELLE',
      justificatifUrl: null,
    };
  }

  soumettrePaiement(): void {
    if (!this.formPaiement.candidatId || !this.formPaiement.montant) return;
    this.tresorerieService.creerPaiement(this.formPaiement).subscribe({
      next: (p) => {
        this.paiements.unshift(p);
        this.showPaiementForm = false;
        this.toast('Paiement enregistré');
        this.loadAll();
      },
      error: () => {
        this.paiementsMock.unshift({ ...this.formPaiement, id: Date.now(), tenantId: 1, candidatNom: 'Candidat', candidatPrenom: '' });
        this.paiements = [...this.paiements];
        this.showPaiementForm = false;
        this.balance = this.buildBalanceMock();
      },
    });
  }

  soumettreCharge(): void {
    if (!this.formCharge.montant) return;
    this.tresorerieService.creerCharge(this.formCharge).subscribe({
      next: (c) => {
        this.charges.unshift(c);
        this.showChargeForm = false;
        this.toast('Charge enregistrée');
        this.loadAll();
      },
      error: () => {
        this.chargesMock.unshift({ ...this.formCharge, id: Date.now(), tenantId: 1 });
        this.charges = [...this.charges];
        this.showChargeForm = false;
        this.balance = this.buildBalanceMock();
      },
    });
  }

  toast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  getTypePaiementBadgeClass(t: TypePaiement): string {
    return t === 'AVANCE' ? 'badge-orange' : 'badge-green';
  }

  getCandidatNomComplet(p: PaiementDTO): string {
    if (p.candidatPrenom || p.candidatNom) return `${p.candidatPrenom ?? ''} ${p.candidatNom ?? ''}`.trim();
    return `Candidat #${p.candidatId}`;
  }

  getPeriodeLabel(p: string): string {
    const map: Record<string, string> = { SEMAINE: 'Semaine', MOIS: 'Mois', TRIMESTRE: 'Trimestre', ANNEE: 'Année' };
    return map[p] ?? p;
  }

  setPeriode(p: string): void {
    this.periode = p as any;
    this.loadAll();
  }

  getPeriodiciteLabel(p: PeriodiciteCharge): string {
    return this.periodicites.find((x) => x.v === p)?.l ?? p;
  }

  getPaiementsLength(): number {
    return this.paiements.length || this.paiementsMock.length;
  }

  getChargesLength(): number {
    return this.charges.length || this.chargesMock.length;
  }

  getPaiementsList(): PaiementDTO[] {
    return this.paiements.length ? this.paiements : this.paiementsMock;
  }

  getChargesList(): ChargeDTO[] {
    return this.charges.length ? this.charges : this.chargesMock;
  }

  private computePeriodeDates(): { debut: string; fin: string } {
    const fin = new Date();
    const debut = new Date();
    switch (this.periode) {
      case 'SEMAINE':
        debut.setDate(fin.getDate() - 7);
        break;
      case 'TRIMESTRE':
        debut.setMonth(fin.getMonth() - 3);
        break;
      case 'ANNEE':
        debut.setFullYear(fin.getFullYear() - 1);
        break;
      case 'MOIS':
      default:
        debut.setMonth(fin.getMonth() - 1);
        break;
    }
    const fmt = (d: Date) => d.toISOString().split('T')[0];
    return { debut: fmt(debut), fin: fmt(fin) };
  }

  exporterComptable(): void {
    const { debut, fin } = this.computePeriodeDates();

    if (this.authService.isDemoMode()) {
      const csv = this.buildCsvExport(debut, fin);
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      this.downloadBlob(blob, `export-comptable-${debut}_${fin}.csv`);
      this.toast('Export généré (mode démo : format CSV)');
      return;
    }

    this.tresorerieService.exporterComptable(debut, fin).subscribe({
      next: (resp) => {
        const blob = resp.body;
        if (blob) {
          const disposition = resp.headers.get('Content-Disposition') ?? '';
          const match = disposition.match(/filename="?([^"]+)"?/);
          const filename = match?.[1] ?? `export-comptable-${debut}_${fin}.xlsx`;
          this.downloadBlob(blob, filename);
          this.toast('Export Excel téléchargé');
        }
      },
      error: () => {
        this.toast("Erreur lors de l'export");
      },
    });
  }

  private buildCsvExport(debut: string, fin: string): string {
    const lines: string[] = [];
    lines.push(`Export comptable;Du ${debut};Au ${fin}`);
    lines.push('');
    lines.push('=== Paiements ===');
    lines.push('Date;Candidat;Type;Mode;Montant (DH)');
    for (const p of this.getPaiementsList()) {
      lines.push(`${p.date};${this.getCandidatNomComplet(p)};${p.type};${this.getModePaiementLabel(p.modePaiement)};${p.montant}`);
    }
    lines.push('');
    lines.push('=== Charges ===');
    lines.push('Date;Categorie;Periodicite;Montant (DH)');
    for (const c of this.getChargesList()) {
      lines.push(`${c.date};${this.getCategorieChargeLabel(c.categorie)};${this.getPeriodiciteLabel(c.periodicite)};${c.montant}`);
    }
    return lines.join('\n');
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }
}
