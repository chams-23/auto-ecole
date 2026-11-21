import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { CandidatService } from '../../core/services/candidat.service';
import { AuthService } from '../../core/services/auth.service';
import { CandidatDTO } from '../../core/models/candidat';

@Component({
  selector: 'app-candidats',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './candidats.component.html',
})
export class CandidatsComponent implements OnInit {
  candidats: CandidatDTO[] = [];
  searchTerm = '';
  loading = false;

  candidatsMock: any[] = [
    { id: 1, nom: 'El Amrani', prenom: 'Youssef', cin: 'AB123456', categoriePermis: 'B', statutParcours: 'ADMIS', heuresConduiteValidees: 20, forfaitHeures: 25, solde: 0, moniteurId: 3 },
    { id: 2, nom: 'Benali', prenom: 'Sara', cin: 'CD789012', categoriePermis: 'C', statutParcours: 'CODE_VALIDE', heuresConduiteValidees: 12, forfaitHeures: 30, solde: 1500, moniteurId: 3 },
    { id: 3, nom: 'Charif', prenom: 'Omar', cin: 'EF345678', categoriePermis: 'B', statutParcours: 'CONVOCATION_CONDUITE', heuresConduiteValidees: 18, forfaitHeures: 25, solde: 0, moniteurId: 4 },
    { id: 4, nom: 'Daoudi', prenom: 'Meryem', cin: 'GH901234', categoriePermis: 'D', statutParcours: 'INSCRIT', heuresConduiteValidees: 5, forfaitHeures: 25, solde: 3500, moniteurId: 4 },
    { id: 5, nom: 'El Fassi', prenom: 'Karim', cin: 'IJ567890', categoriePermis: 'B', statutParcours: 'DOSSIER_COMPLET', heuresConduiteValidees: 0, forfaitHeures: 25, solde: 2000, moniteurId: 3 },
    { id: 6, nom: 'Gharbi', prenom: 'Fatima', cin: 'KL123890', categoriePermis: 'EB', statutParcours: 'REFUSE', heuresConduiteValidees: 15, forfaitHeures: 25, solde: 500, moniteurId: 4 },
  ];

  constructor(
    private candidatService: CandidatService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCandidats();
  }

  canWrite(): boolean { return this.authService.canWrite('candidats'); }
  isMoniteurRole(): boolean { return this.authService.isMoniteur(); }

  getCurrentUserId(): number {
    return this.authService.getCurrentUser()?.id ?? 0;
  }

  loadCandidats(): void {
    this.loading = true;
    this.candidatService.lister().subscribe({
      next: (data) => {
        this.candidats = this.appliquerFiltresRole(data);
        this.loading = false;
      },
      error: () => {
        this.candidats = this.appliquerFiltresRole(this.candidatsMock);
        this.loading = false;
      }
    });
  }

  private appliquerFiltresRole(liste: any[]): any[] {
    if (this.authService.isMoniteur()) {
      const moniteurUserId = this.getCurrentUserId();
      return liste.filter(c => c.moniteurId == null || c.moniteurId === moniteurUserId);
    }
    return liste;
  }

  get candidatsFiltres(): any[] {
    const list = this.candidats.length > 0 ? this.candidats : this.appliquerFiltresRole(this.candidatsMock);
    if (!this.searchTerm) return list;
    const q = this.searchTerm.toLowerCase();
    return list.filter((c: any) =>
      (c.nom + ' ' + c.prenom).toLowerCase().includes(q) ||
      (c.cin || '').toLowerCase().includes(q)
    );
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

  formatStatut(statut: string): string {
    return statut.split('_').map(s => s.charAt(0) + s.slice(1).toLowerCase()).join(' ');
  }

  getStatutDisplay(statut: string): { class: string; text: string } {
    switch (statut) {
      case 'ADMIS':
        return { class: 'status-go', text: '✓ ADMIS' };
      case 'REFUSE':
        return { class: 'status-stop', text: '✕ REFUSÉ' };
      case 'INSCRIT':
      case 'DOSSIER_COMPLET':
        return { class: 'status-warn', text: `⚠ ${this.formatStatut(statut)}` };
      case 'CONVOCATION_CODE':
      case 'CODE_VALIDE':
      case 'CONVOCATION_CONDUITE':
        return { class: 'status-warn', text: `→ ${this.formatStatut(statut)}` };
      default:
        return { class: 'status-warn', text: this.formatStatut(statut) };
    }
  }

  getHeuresForfait(c: any): string {
    const effectuees = c.heuresConduiteValidees || 0;
    const forfait = c.forfaitHeures || 25;
    return `${effectuees}h / ${forfait}h`;
  }

  getSoldeColor(solde: number): string {
    if (solde === 0) return 'text-go';
    if (solde < 1000) return 'text-orange';
    return 'text-stop';
  }

  getSoldeText(solde: number): string {
    if (solde === 0) return 'Soldé';
    return `${solde.toLocaleString('fr-FR')} DH`;
  }

  voirDetail(id: number): void {
    this.router.navigate(['/candidats', id]);
  }

  editer(id: number): void {
    if (!this.canWrite()) return;
    this.router.navigate(['/candidats', id, 'modifier']);
  }
}
