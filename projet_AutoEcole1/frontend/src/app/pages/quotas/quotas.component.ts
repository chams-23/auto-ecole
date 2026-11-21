import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { QuotaHeuresService } from '../../core/services/quota-heures.service';
import { AuthService } from '../../core/services/auth.service';
import { QuotaHeuresDTO, UpsertQuotaHeuresRequest, CATEGORIES_PERMIS } from '../../core/models/quota-heures';

@Component({
  selector: 'app-quotas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quotas.component.html',
})
export class QuotasComponent implements OnInit {
  quotas: QuotaHeuresDTO[] = [];
  loading = false;
  showForm = false;
  modeForm: 'CREER' | 'MODIFIER' = 'CREER';
  selectedId: number | null = null;
  successMessage = '';
  errorMessage = '';

  categoriesDisponibles = CATEGORIES_PERMIS;

  formQuota: {
    categoriePermis: string;
    heuresCodeMin: number | null;
    heuresConduiteMin: number | null;
  } = {
    categoriePermis: 'B',
    heuresCodeMin: null,
    heuresConduiteMin: null,
  };

  constructor(
    private quotaHeuresService: QuotaHeuresService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadQuotas();
  }

  canWrite(): boolean { return this.authService.canWrite('quotas'); }

  loadQuotas(): void {
    this.loading = true;
    this.quotaHeuresService.lister().subscribe({
      next: (data) => {
        this.quotas = data;
        this.loading = false;
      },
      error: () => {
        this.quotas = this.buildMockQuotas();
        this.loading = false;
      }
    });
  }

  private buildMockQuotas(): QuotaHeuresDTO[] {
    return CATEGORIES_PERMIS.slice(0, 5).map((c, i) => ({
      id: i + 1,
      tenantId: 1,
      categoriePermis: c.v,
      heuresCodeMin: c.defaultCode,
      heuresConduiteMin: c.defaultConduite,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }));
  }

  categoriesManquantes(): typeof CATEGORIES_PERMIS {
    const existantes = new Set(this.quotas.map(q => q.categoriePermis));
    return CATEGORIES_PERMIS.filter(c => !existantes.has(c.v));
  }

  getCategorieLabel(v: string): string {
    return CATEGORIES_PERMIS.find(x => x.v === v)?.l ?? v;
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

  getDefaultForCategory(cat: string): { code: number; conduite: number } {
    const c = CATEGORIES_PERMIS.find(x => x.v === cat);
    return c ? { code: c.defaultCode, conduite: c.defaultConduite } : { code: 0, conduite: 0 };
  }

  ouvrirCreer(): void {
    if (!this.canWrite()) return;
    this.modeForm = 'CREER';
    this.selectedId = null;
    this.errorMessage = '';
    const premiereCat = this.categoriesManquantes()[0]?.v ?? 'B';
    const defaut = this.getDefaultForCategory(premiereCat);
    this.formQuota = {
      categoriePermis: premiereCat,
      heuresCodeMin: defaut.code,
      heuresConduiteMin: defaut.conduite,
    };
    this.showForm = true;
  }

  ouvrirModifier(q: QuotaHeuresDTO): void {
    if (!this.canWrite()) return;
    this.modeForm = 'MODIFIER';
    this.selectedId = q.id;
    this.errorMessage = '';
    this.formQuota = {
      categoriePermis: q.categoriePermis,
      heuresCodeMin: q.heuresCodeMin,
      heuresConduiteMin: q.heuresConduiteMin,
    };
    this.showForm = true;
  }

  fermerForm(): void {
    this.showForm = false;
    this.selectedId = null;
    this.errorMessage = '';
  }

  onChangerCategorie(): void {
    const defaut = this.getDefaultForCategory(this.formQuota.categoriePermis);
    if (this.modeForm === 'CREER') {
      this.formQuota.heuresCodeMin = defaut.code;
      this.formQuota.heuresConduiteMin = defaut.conduite;
    }
  }

  formEstValide(): boolean {
    const f = this.formQuota;
    if (!f.categoriePermis) return false;
    if (f.heuresCodeMin === null || f.heuresCodeMin === undefined) return false;
    if (f.heuresConduiteMin === null || f.heuresConduiteMin === undefined) return false;
    if (f.heuresCodeMin < 0 || f.heuresConduiteMin < 0) return false;
    return true;
  }

  soumettreForm(): void {
    if (!this.formEstValide()) return;
    this.errorMessage = '';

    const req: UpsertQuotaHeuresRequest = {
      categoriePermis: this.formQuota.categoriePermis,
      heuresCodeMin: this.formQuota.heuresCodeMin,
      heuresConduiteMin: this.formQuota.heuresConduiteMin,
    };

    if (this.modeForm === 'CREER') {
      this.quotaHeuresService.creer(req).subscribe({
        next: (q) => {
          this.quotas.push(q);
          this.showForm = false;
          this.toast('Quota créé avec succès');
          this.loadQuotas();
        },
        error: (err) => {
          const msg = err?.error?.message ?? 'Erreur lors de la création (conflit de catégorie possible)';
          if (this.authService.isDemoMode()) {
            const nouveau: QuotaHeuresDTO = {
              id: Date.now(),
              tenantId: 1,
              categoriePermis: req.categoriePermis,
              heuresCodeMin: req.heuresCodeMin ?? 0,
              heuresConduiteMin: req.heuresConduiteMin ?? 0,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            this.quotas.push(nouveau);
            this.showForm = false;
            this.toast('Quota créé (mode démo)');
          } else {
            this.errorMessage = msg;
          }
        },
      });
    } else {
      const id = this.selectedId!;
      this.quotaHeuresService.modifier(id, req).subscribe({
        next: (q) => {
          const idx = this.quotas.findIndex(x => x.id === id);
          if (idx >= 0) this.quotas[idx] = q;
          this.showForm = false;
          this.toast('Quota modifié avec succès');
        },
        error: (err) => {
          const msg = err?.error?.message ?? 'Erreur lors de la modification';
          if (this.authService.isDemoMode()) {
            const idx = this.quotas.findIndex(x => x.id === id);
            if (idx >= 0) {
              this.quotas[idx] = {
                ...this.quotas[idx],
                heuresCodeMin: req.heuresCodeMin ?? 0,
                heuresConduiteMin: req.heuresConduiteMin ?? 0,
                updatedAt: new Date().toISOString(),
              };
            }
            this.showForm = false;
            this.toast('Quota modifié (mode démo)');
          } else {
            this.errorMessage = msg;
          }
        },
      });
    }
  }

  supprimer(q: QuotaHeuresDTO): void {
    if (!this.canWrite()) return;
    if (!confirm(`Supprimer le quota pour la catégorie ${q.categoriePermis} ? Cette action est irréversible.`)) return;
    this.quotaHeuresService.supprimer(q.id).subscribe({
      next: () => {
        this.quotas = this.quotas.filter(x => x.id !== q.id);
        this.toast('Quota supprimé');
      },
      error: () => {
        this.quotas = this.quotas.filter(x => x.id !== q.id);
        this.toast('Quota supprimé (mode démo)');
      },
    });
  }

  toast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }
}
