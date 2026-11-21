import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { CandidatService } from '../../core/services/candidat.service';
import { TresorerieService } from '../../core/services/tresorerie.service';
import { SeanceService } from '../../core/services/seance.service';
import { DocumentCandidatService } from '../../core/services/document-candidat.service';
import { AuthService } from '../../core/services/auth.service';
import { CandidatDTO, CompteurHeures, SoldeCandidat, StatutQuotaHeures } from '../../core/models/candidat';
import { PaiementDTO } from '../../core/models/tresorerie';
import { SeanceDTO } from '../../core/models/seance';
import {
  DocumentCandidatDTO,
  UploadDocumentCandidatRequest,
  TypeDocumentCandidat,
  StatutValidationDocument,
} from '../../core/models/document-candidat';

@Component({
  selector: 'app-candidat-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './candidat-detail.component.html',
})
export class CandidatDetailComponent implements OnInit {
  candidatId: number;
  candidat?: CandidatDTO;
  compteur?: CompteurHeures;
  solde?: SoldeCandidat;
  statutQuota?: StatutQuotaHeures;
  paiements: PaiementDTO[] = [];
  seances: SeanceDTO[] = [];
  documents: DocumentCandidatDTO[] = [];
  loading = true;
  activeTab = 'general';
  showUploadForm = false;
  operationMsg = '';
  operationError = '';
  editingStatut = false;
  statutEditValue = '';
  availableStatuts: string[] = [
    'INSCRIT', 'DOSSIER_COMPLET', 'CONVOCATION_CODE', 'CODE_VALIDE',
    'CONVOCATION_CONDUITE', 'PRESENTE_EXAMEN', 'ADMIS', 'REFUSE'
  ];

  uploadForm: UploadDocumentCandidatRequest = {
    typeDocument: 'CIN_COPIE',
    nomFichier: '',
    urlFichier: null,
    dateExpiration: null,
  };

  typesDocuments: { v: TypeDocumentCandidat; l: string }[] = [
    { v: 'CNE', l: 'CNE' },
    { v: 'CIN_COPIE', l: 'Copie CIN' },
    { v: 'CERTIFICAT_RESIDENCE', l: 'Certificat de résidence' },
    { v: 'CERTIFICAT_SCOLARITE', l: 'Certificat de scolarité' },
    { v: 'CERTIFICAT_MEDICAL', l: 'Certificat médical' },
    { v: 'PHOTO_IDENTITE', l: 'Photo identité' },
    { v: 'ATTESTATION_INSCRIPTION', l: 'Attestation inscription' },
    { v: 'PERMIS_PROVISOIRE', l: 'Permis provisoire' },
    { v: 'AUTRE', l: 'Autre' },
  ];

  mockDocuments: DocumentCandidatDTO[] = [
    { id: 1, candidatId: 1, tenantId: 1, typeDocument: 'CIN_COPIE', nomFichier: 'cin_youssef.pdf', urlFichier: null, statut: 'VALIDE', dateUpload: new Date(Date.now() - 10 * 86400000).toISOString(), dateExpiration: null, uploadParUtilisateurId: 2, valideParUtilisateurId: 2, dateValidation: new Date(Date.now() - 9 * 86400000).toISOString() },
    { id: 2, candidatId: 1, tenantId: 1, typeDocument: 'CERTIFICAT_MEDICAL', nomFichier: 'certif_medical.pdf', urlFichier: null, statut: 'EN_ATTENTE', dateUpload: new Date(Date.now() - 2 * 86400000).toISOString(), dateExpiration: null, uploadParUtilisateurId: 6 },
    { id: 3, candidatId: 1, tenantId: 1, typeDocument: 'PHOTO_IDENTITE', nomFichier: 'photo.jpg', urlFichier: null, statut: 'REFUSE', dateUpload: new Date(Date.now() - 5 * 86400000).toISOString(), dateExpiration: null, uploadParUtilisateurId: 6, valideParUtilisateurId: 2, dateValidation: new Date(Date.now() - 4 * 86400000).toISOString(), commentaire: 'Photo floue, merci de renvoyer une version nette' },
  ];

  mockCandidat: CandidatDTO = {
    id: 1,
    utilisateurId: 1,
    tenantId: 1,
    nom: 'El Amrani',
    prenom: 'Youssef',
    email: 'youssef.elamrani@email.com',
    telephone: '0612345678',
    cin: 'AB123456',
    categoriePermis: 'B',
    statutParcours: 'ADMIS',
    forfaitId: 1,
    heuresConduiteValidees: 20,
    heuresTheorieValidees: 8
  };

  mockCompteur: CompteurHeures = {
    heuresConduiteValidees: 20,
    heuresTheorieValidees: 8,
    heuresConduiteRestantes: 5,
    heuresTheorieRestantes: 2
  };

  mockSolde: SoldeCandidat = {
    totalDu: 3500,
    totalPaye: 3500,
    soldeRestant: 0
  };

  mockStatutQuota: StatutQuotaHeures = {
    categoriePermis: 'B',
    quotaCodeMin: 60,
    quotaConduiteMin: 20,
    heuresCodeValidees: 8,
    heuresConduiteValidees: 20,
    codeAtteint: false,
    conduiteAtteint: true,
    quotaAtteint: false,
  };

  constructor(
    private route: ActivatedRoute,
    private candidatService: CandidatService,
    private tresorerieService: TresorerieService,
    private seanceService: SeanceService,
    private documentCandidatService: DocumentCandidatService,
    public readonly authService: AuthService
  ) {
    this.candidatId = +this.route.snapshot.paramMap.get('id')!;
  }

  ngOnInit(): void {
    this.loadAll();
  }

  canUploadDocument(): boolean {
    return this.authService.isAdmin() || this.authService.isSecretaire() || this.authService.isCandidat();
  }

  canValidateDocument(): boolean {
    return this.authService.isAdmin() || this.authService.isSecretaire();
  }

  canEditStatut(): boolean {
    return this.authService.isAdmin() || this.authService.isSecretaire();
  }

  loadAll(): void {
    this.candidatService.getById(this.candidatId).subscribe({
      next: (c) => { this.candidat = c; this.statutEditValue = c.statutParcours; this.checkLoadComplete(); },
      error: () => { this.candidat = this.mockCandidat; this.statutEditValue = this.mockCandidat.statutParcours; this.checkLoadComplete(); }
    });
    this.candidatService.compteurHeures(this.candidatId).subscribe({
      next: (c) => { this.compteur = c; this.checkLoadComplete(); },
      error: () => { this.compteur = this.mockCompteur; this.checkLoadComplete(); }
    });
    this.candidatService.solde(this.candidatId).subscribe({
      next: (s) => { this.solde = s; this.checkLoadComplete(); },
      error: () => { this.solde = this.mockSolde; this.checkLoadComplete(); }
    });
    this.candidatService.statutQuota(this.candidatId).subscribe({
      next: (q) => { this.statutQuota = q; this.checkLoadComplete(); },
      error: () => { this.statutQuota = this.mockStatutQuota; this.checkLoadComplete(); }
    });
    this.tresorerieService.listerPaiements(this.candidatId).subscribe({
      next: (p) => { this.paiements = p; this.checkLoadComplete(); },
      error: () => { this.paiements = []; this.checkLoadComplete(); }
    });
    this.seanceService.filtrer({ candidat_id: this.candidatId }).subscribe({
      next: (s) => { this.seances = s; this.checkLoadComplete(); },
      error: () => { this.seances = []; this.checkLoadComplete(); }
    });
    this.documentCandidatService.listerParCandidat(this.candidatId).subscribe({
      next: (d) => { this.documents = d; this.checkLoadComplete(); },
      error: () => { this.documents = this.mockDocuments; this.checkLoadComplete(); }
    });
  }

  private loadCount: number = 0;
  private readonly totalLoads: number = 7;
  private checkLoadComplete(): void {
    this.loadCount++;
    if (this.loadCount >= this.totalLoads) {
      this.loading = false;
    }
  }

  getTypeDocLabel(t: TypeDocumentCandidat): string {
    return this.typesDocuments.find(x => x.v === t)?.l ?? t;
  }

  getStatutDocBadgeClass(s: StatutValidationDocument): string {
    switch (s) {
      case 'VALIDE': return 'status-go';
      case 'REFUSE': return 'status-stop';
      case 'EXPIRIE': return 'status-stop';
      default: return 'status-warn';
    }
  }

  formatStatutDoc(s: StatutValidationDocument): string {
    switch (s) {
      case 'EN_ATTENTE': return '⏳ En attente';
      case 'VALIDE': return '✓ Validé';
      case 'REFUSE': return '✕ Refusé';
      case 'EXPIRIE': return '⟳ Expiré';
      default: return s;
    }
  }

  ouvrirUpload(): void {
    this.showUploadForm = true;
    this.operationMsg = '';
    this.uploadForm = {
      typeDocument: 'CIN_COPIE',
      nomFichier: '',
      urlFichier: null,
      dateExpiration: null,
    };
  }

  fermerUpload(): void {
    this.showUploadForm = false;
    this.operationMsg = '';
  }

  soumettreUpload(): void {
    if (!this.uploadForm.nomFichier.trim()) return;

    if (this.authService.isDemoMode()) {
      const nouveau: DocumentCandidatDTO = {
        id: Date.now(),
        candidatId: this.candidatId,
        tenantId: 1,
        typeDocument: this.uploadForm.typeDocument,
        nomFichier: this.uploadForm.nomFichier,
        urlFichier: this.uploadForm.urlFichier,
        statut: 'EN_ATTENTE',
        dateUpload: new Date().toISOString(),
        dateExpiration: this.uploadForm.dateExpiration,
        uploadParUtilisateurId: this.authService.getCurrentUserId() ?? null,
      };
      this.documents.unshift(nouveau);
      this.fermerUpload();
      this.flashMsg('Document téléchargé');
      return;
    }

    this.documentCandidatService.uploaderDocument(this.candidatId, this.uploadForm).subscribe({
      next: (d) => {
        this.documents.unshift(d);
        this.fermerUpload();
        this.flashMsg('Document téléchargé');
      },
      error: () => {
        this.operationError = "Erreur lors du téléversement";
      }
    });
  }

  validerDocument(id: number): void {
    const action = () => {
      if (this.authService.isDemoMode()) {
        const doc = this.documents.find(d => d.id === id);
        if (doc) { doc.statut = 'VALIDE'; doc.dateValidation = new Date().toISOString(); }
        this.flashMsg('Document validé');
        return;
      }
      this.documentCandidatService.validerDocument(id).subscribe({
        next: (d) => { this.remplacerDocument(d); this.flashMsg('Document validé'); },
        error: () => {}
      });
    };
    action();
  }

  refuserDocument(id: number): void {
    const commentaire = prompt('Motif du refus (optionnel) :') ?? undefined;
    const payload = commentaire ? { commentaire } : undefined;

    if (this.authService.isDemoMode()) {
      const doc = this.documents.find(d => d.id === id);
      if (doc) { doc.statut = 'REFUSE'; doc.dateValidation = new Date().toISOString(); doc.commentaire = commentaire ?? null; }
      this.flashMsg('Document refusé');
      return;
    }
    this.documentCandidatService.refuserDocument(id, payload).subscribe({
      next: (d) => { this.remplacerDocument(d); this.flashMsg('Document refusé'); },
      error: () => {}
    });
  }

  private remplacerDocument(d: DocumentCandidatDTO): void {
    const i = this.documents.findIndex(x => x.id === d.id);
    if (i >= 0) this.documents[i] = d;
  }

  supprimerDocument(id: number): void {
    if (!confirm('Supprimer ce document ?')) return;
    if (this.authService.isDemoMode()) {
      this.documents = this.documents.filter(d => d.id !== id);
      this.flashMsg('Document supprimé');
      return;
    }
    this.documentCandidatService.supprimer(id).subscribe({
      next: () => { this.documents = this.documents.filter(d => d.id !== id); this.flashMsg('Document supprimé'); },
      error: () => {}
    });
  }

  private flashMsg(msg: string, isError = false): void {
    if (isError) {
      this.operationError = msg;
      setTimeout(() => { this.operationError = ''; }, 3500);
    } else {
      this.operationMsg = msg;
      setTimeout(() => { this.operationMsg = ''; }, 2500);
    }
  }

  formatDate(d: string): string {
    if (!d) return '';
    try {
      return new Date(d + 'T00:00:00').toLocaleDateString('fr-FR');
    } catch {
      return d;
    }
  }

  getCategorieBadgeClass(cat: string): string {
    switch (cat) {
      case 'B': return 'badge-green';
      case 'C': return 'badge-blue';
      case 'D': return 'badge-purple';
      case 'EB':
      case 'EC':
      case 'ED': return 'badge-orange';
      case 'A': return 'badge-gray';
      default: return 'badge-gray';
    }
  }

  getStatutDisplay(statut: string): { class: string; text: string } {
    switch (statut) {
      case 'ADMIS':
        return { class: 'status-go', text: '✓ ADMIS' };
      case 'REFUSE':
        return { class: 'status-stop', text: '✕ REFUSÉ' };
      case 'PRESENTE_EXAMEN':
        return { class: 'status-go', text: '🏁 PRÉSENTÉ À L\'EXAMEN' };
      case 'INSCRIT':
      case 'DOSSIER_COMPLET':
        return { class: 'status-warn', text: `⚠ ${statut.replace(/_/g, ' ')}` };
      case 'CONVOCATION_CODE':
      case 'CODE_VALIDE':
      case 'CONVOCATION_CONDUITE':
        return { class: 'status-warn', text: `→ ${statut.replace(/_/g, ' ')}` };
      default:
        return { class: 'status-warn', text: statut };
    }
  }

  getStatutSeanceClass(statut: string): string {
    switch (statut) {
      case 'VALIDEE': return 'status-go';
      case 'VERROUILLEE': return 'status-go';
      case 'EN_ATTENTE': return 'status-warn';
      case 'DECLAREE_EFFECTUEE': return 'status-warn';
      case 'CONTESTEE': return 'status-stop';
      default: return 'status-warn';
    }
  }

  getPourcentageTheorie(): number {
    if (!this.compteur) return 0;
    const total = this.compteur.heuresTheorieValidees + this.compteur.heuresTheorieRestantes;
    if (!total) return 0;
    return Math.min(100, Math.round((this.compteur.heuresTheorieValidees / total) * 100));
  }

  getPourcentageConduite(): number {
    if (!this.compteur) return 0;
    const total = this.compteur.heuresConduiteValidees + this.compteur.heuresConduiteRestantes;
    if (!total) return 0;
    return Math.min(100, Math.round((this.compteur.heuresConduiteValidees / total) * 100));
  }

  getTotalTheorie(): number {
    if (!this.compteur) return 0;
    return this.compteur.heuresTheorieValidees + this.compteur.heuresTheorieRestantes;
  }

  getTotalConduite(): number {
    if (!this.compteur) return 0;
    return this.compteur.heuresConduiteValidees + this.compteur.heuresConduiteRestantes;
  }

  getRestantColor(): string {
    const r = this.solde?.soldeRestant ?? 0;
    if (r === 0) return 'text-go';
    if (r < 1000) return 'text-orange';
    return 'text-stop';
  }

  formatMontant(m: number): string {
    return `${m.toLocaleString('fr-FR')} DH`;
  }

  formatHeure(h: string): string {
    if (!h) return '';
    return h.substring(0, 5);
  }

  formatStatut(statut: string): string {
    if (!statut) return '';
    return statut.split('_').join(' ');
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  getPourcentageQuotaCode(): number {
    if (!this.statutQuota) return 0;
    const total = this.statutQuota.quotaCodeMin || 1;
    return Math.min(100, Math.round((this.statutQuota.heuresCodeValidees / total) * 100));
  }

  getPourcentageQuotaConduite(): number {
    if (!this.statutQuota) return 0;
    const total = this.statutQuota.quotaConduiteMin || 1;
    return Math.min(100, Math.round((this.statutQuota.heuresConduiteValidees / total) * 100));
  }

  getStatutQuotaBadge(): { class: string; text: string } {
    if (!this.statutQuota) return { class: 'status-warn', text: '—' };
    if (this.statutQuota.quotaAtteint) {
      return { class: 'status-go', text: '✓ Quota CNEP atteint' };
    }
    return { class: 'status-stop', text: '⚠ Quota CNEP non atteint' };
  }

  debuterEditionStatut(): void {
    if (!this.canEditStatut() || !this.candidat) return;
    this.statutEditValue = this.candidat.statutParcours;
    this.editingStatut = true;
    this.operationError = '';
  }

  annulerEditionStatut(): void {
    this.editingStatut = false;
    this.operationError = '';
    if (this.candidat) this.statutEditValue = this.candidat.statutParcours;
  }

  confirmerChangementStatut(): void {
    if (!this.candidat) return;
    const nouveau = this.statutEditValue;
    const actuel = this.candidat.statutParcours;
    if (nouveau === actuel) {
      this.editingStatut = false;
      return;
    }

    if (nouveau === 'PRESENTE_EXAMEN' && this.statutQuota && !this.statutQuota.quotaAtteint) {
      const bloquer = !confirm(
        `⚠ QUOTA CNEP NON ATTEINT !\n\n` +
        `Code: ${this.statutQuota.heuresCodeValidees}/${this.statutQuota.quotaCodeMin} h ` +
        `(${this.statutQuota.codeAtteint ? '✓' : '✗'})\n` +
        `Conduite: ${this.statutQuota.heuresConduiteValidees}/${this.statutQuota.quotaConduiteMin} h ` +
        `(${this.statutQuota.conduiteAtteint ? '✓' : '✗'})\n\n` +
        `Le serveur risque de refuser ce changement. Confirmer quand même ?`
      );
      if (bloquer) return;
    }

    const dto: Partial<CandidatDTO> = { statutParcours: nouveau };
    this.candidatService.modifier(this.candidatId, dto).subscribe({
      next: (c) => {
        this.candidat = c;
        this.editingStatut = false;
        this.flashMsg(`Statut changé : ${this.formatStatut(c.statutParcours)}`);
        if (nouveau === 'PRESENTE_EXAMEN') {
          this.candidatService.statutQuota(this.candidatId).subscribe({
            next: (q) => { this.statutQuota = q; },
            error: () => {}
          });
        }
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Erreur lors du changement de statut';
        this.flashMsg(msg, true);
      }
    });
  }
}
