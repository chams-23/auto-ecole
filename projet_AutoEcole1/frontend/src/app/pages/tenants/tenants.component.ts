import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { TenantService } from '../../core/services/tenant.service';
import { AuthService } from '../../core/services/auth.service';
import {
  Tenant,
  CreateTenantRequest,
  StatutAbonnement,
  PlanAbonnement,
} from '../../core/models/tenant';

@Component({
  selector: 'app-tenants',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './tenants.component.html',
})
export class TenantsComponent implements OnInit {
  tenants: Tenant[] = [];
  searchTerm = '';
  loading = false;
  successMessage = '';

  tenantsMock: Tenant[] = [
    {
      id: 1,
      raisonSociale: 'Auto-École Chaimaa',
      numeroAgrement: 'AE-440-00123',
      wilaya: 'Chaimaa',
      statutAbonnement: 'ACTIF',
      planAbonnement: 'PREMIUM',
      dateDebutAbonnement: '2025-01-15',
      dateFinAbonnement: '2026-01-15',
      montantAbonnement: 12000,
      createdAt: '2025-01-10T09:30:00Z',
    },
    {
      id: 2,
      raisonSociale: 'Auto-École Safi Road',
      numeroAgrement: 'AE-460-00456',
      wilaya: 'Safi',
      statutAbonnement: 'SUSPENDU',
      planAbonnement: 'PROFESSIONNEL',
      dateDebutAbonnement: '2025-03-01',
      dateFinAbonnement: '2026-03-01',
      montantAbonnement: 8400,
      createdAt: '2025-02-20T11:15:00Z',
    },
    {
      id: 3,
      raisonSociale: 'Auto-École Casa Center',
      numeroAgrement: 'AE-110-00789',
      wilaya: 'Casablanca',
      statutAbonnement: 'RESILIE',
      planAbonnement: 'ESSENTIEL',
      dateDebutAbonnement: '2024-06-01',
      dateFinAbonnement: '2025-06-01',
      montantAbonnement: 4800,
      createdAt: '2024-05-15T14:45:00Z',
    },
  ];

  showCreateForm = false;
  formCreate: CreateTenantRequest = {
    raisonSociale: '',
    numeroAgrement: '',
    wilaya: '',
    planAbonnement: 'ESSENTIEL',
    dateDebutAbonnement: new Date().toISOString().split('T')[0],
    dateFinAbonnement: new Date(Date.now() + 365 * 86400000).toISOString().split('T')[0],
    montantAbonnement: 0,
  };

  showSuspendreModal: { [id: number]: boolean } = {};
  dateSuspensionTemp: { [id: number]: string } = {};

  showResilierModal: { [id: number]: boolean } = {};
  dateResiliationTemp: { [id: number]: string } = {};

  showPlanModal: { [id: number]: boolean } = {};
  planTemp: { [id: number]: PlanAbonnement } = {} as any;
  debutTemp: { [id: number]: string } = {};
  finTemp: { [id: number]: string } = {};
  montantTemp: { [id: number]: number } = {} as any;

  statutsAbonnement: { v: StatutAbonnement; l: string }[] = [
    { v: 'ACTIF', l: 'Actif' },
    { v: 'SUSPENDU', l: 'Suspendu' },
    { v: 'RESILIE', l: 'Résilié' },
  ];

  plansAbonnement: { v: PlanAbonnement; l: string }[] = [
    { v: 'ESSENTIEL', l: 'Essentiel' },
    { v: 'PROFESSIONNEL', l: 'Professionnel' },
    { v: 'PREMIUM', l: 'Premium' },
  ];

  constructor(
    private tenantService: TenantService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTenants();
  }

  canWrite(): boolean { return this.authService.canWrite('tenants'); }

  loadTenants(): void {
    this.loading = true;
    this.tenantService.listerTous().subscribe({
      next: (data) => {
        this.tenants = data;
        this.loading = false;
      },
      error: () => {
        this.tenants = this.tenantsMock;
        this.loading = false;
      }
    });
  }

  get tenantsFiltres(): Tenant[] {
    const list = this.tenants.length > 0 ? this.tenants : this.tenantsMock;
    if (!this.searchTerm) return list;
    const q = this.searchTerm.toLowerCase();
    return list.filter((t: Tenant) =>
      (t.raisonSociale || '').toLowerCase().includes(q) ||
      (t.numeroAgrement || '').toLowerCase().includes(q) ||
      (t.wilaya || '').toLowerCase().includes(q)
    );
  }

  getStatutDisplay(statut: StatutAbonnement): { class: string; text: string } {
    switch (statut) {
      case 'ACTIF':
        return { class: 'status-go', text: '✓ ACTIF' };
      case 'SUSPENDU':
        return { class: 'status-warn', text: '⚠ SUSPENDU' };
      case 'RESILIE':
        return { class: 'status-stop', text: '✕ RÉSILIÉ' };
      default:
        return { class: 'status-warn', text: statut };
    }
  }

  getPlanBadgeClass(plan?: PlanAbonnement | null): string {
    switch (plan) {
      case 'PREMIUM': return 'badge-purple';
      case 'PROFESSIONNEL': return 'badge-blue';
      case 'ESSENTIEL': return 'badge-green';
      default: return 'badge-gray';
    }
  }

  getPlanLabel(plan?: PlanAbonnement | null): string {
    return this.plansAbonnement.find((x) => x.v === plan)?.l ?? (plan ?? '—');
  }

  getStatutLabel(statut: StatutAbonnement): string {
    return this.statutsAbonnement.find((x) => x.v === statut)?.l ?? statut;
  }

  formatDate(d?: string | null): string {
    if (!d) return '—';
    return new Date(d + 'T00:00:00').toLocaleDateString('fr-FR');
  }

  getPeriode(t: Tenant): string {
    if (!t.dateDebutAbonnement && !t.dateFinAbonnement) return '—';
    return `${this.formatDate(t.dateDebutAbonnement)} → ${this.formatDate(t.dateFinAbonnement)}`;
  }

  getMontantDH(m?: number | null): string {
    if (m == null) return '—';
    return `${m.toLocaleString('fr-FR')} DH`;
  }

  ouvrirCreation(): void {
    this.showCreateForm = true;
    this.formCreate = {
      raisonSociale: '',
      numeroAgrement: '',
      wilaya: '',
      planAbonnement: 'ESSENTIEL',
      dateDebutAbonnement: new Date().toISOString().split('T')[0],
      dateFinAbonnement: new Date(Date.now() + 365 * 86400000).toISOString().split('T')[0],
      montantAbonnement: 0,
    };
  }

  soumettreCreation(): void {
    if (!this.formCreate.raisonSociale || !this.formCreate.numeroAgrement || !this.formCreate.wilaya || !this.formCreate.montantAbonnement) return;
    this.tenantService.creer(this.formCreate).subscribe({
      next: (t) => {
        this.tenants.unshift(t);
        this.showCreateForm = false;
        this.toast('Auto-école créée avec succès');
        this.loadTenants();
      },
      error: () => {
        this.tenantsMock.unshift({
          id: Date.now(),
          ...this.formCreate,
          statutAbonnement: 'ACTIF',
          createdAt: new Date().toISOString(),
        });
        this.tenants = [...this.tenants];
        this.showCreateForm = false;
        this.toast('Auto-école créée (mode démo)');
      },
    });
  }

  activer(id: number): void {
    this.tenantService.activerAbonnement(id).subscribe({
      next: (t) => {
        this.mettreAJourTenant(t);
        this.toast('Abonnement activé');
      },
      error: () => {
        const t = this.trouverTenant(id);
        if (t) { t.statutAbonnement = 'ACTIF'; this.tenants = [...this.tenants]; }
        this.toast('Abonnement activé (mode démo)');
      },
    });
  }

  ouvrirSuspendre(id: number): void {
    this.showSuspendreModal[id] = true;
    this.dateSuspensionTemp[id] = new Date().toISOString().split('T')[0];
  }

  fermerSuspendre(id: number): void {
    this.showSuspendreModal[id] = false;
  }

  confirmerSuspendre(id: number): void {
    const ds = this.dateSuspensionTemp[id];
    if (!ds) return;
    this.tenantService.suspendreAbonnement(id, ds).subscribe({
      next: (t) => {
        this.mettreAJourTenant(t);
        this.showSuspendreModal[id] = false;
        this.toast('Abonnement suspendu');
      },
      error: () => {
        const t = this.trouverTenant(id);
        if (t) { t.statutAbonnement = 'SUSPENDU'; this.tenants = [...this.tenants]; }
        this.showSuspendreModal[id] = false;
        this.toast('Abonnement suspendu (mode démo)');
      },
    });
  }

  ouvrirResilier(id: number): void {
    this.showResilierModal[id] = true;
    this.dateResiliationTemp[id] = new Date().toISOString().split('T')[0];
  }

  fermerResilier(id: number): void {
    this.showResilierModal[id] = false;
  }

  confirmerResilier(id: number): void {
    const dr = this.dateResiliationTemp[id];
    if (!dr) return;
    this.tenantService.resilierAbonnement(id, dr).subscribe({
      next: (t) => {
        this.mettreAJourTenant(t);
        this.showResilierModal[id] = false;
        this.toast('Abonnement résilié');
      },
      error: () => {
        const t = this.trouverTenant(id);
        if (t) { t.statutAbonnement = 'RESILIE'; this.tenants = [...this.tenants]; }
        this.showResilierModal[id] = false;
        this.toast('Abonnement résilié (mode démo)');
      },
    });
  }

  changementStatutDirect(id: number, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const nouveauStatut = select.value as StatutAbonnement;
    this.tenantService.modifierStatutAbonnement(id, nouveauStatut).subscribe({
      next: (t) => {
        this.mettreAJourTenant(t);
        this.toast('Statut modifié');
      },
      error: () => {
        const t = this.trouverTenant(id);
        if (t) { t.statutAbonnement = nouveauStatut; this.tenants = [...this.tenants]; }
        this.toast('Statut modifié (mode démo)');
      },
    });
  }

  ouvrirModifierPlan(id: number): void {
    const t = this.trouverTenant(id);
    this.showPlanModal[id] = true;
    this.planTemp[id] = t?.planAbonnement ?? 'ESSENTIEL';
    this.debutTemp[id] = t?.dateDebutAbonnement ?? new Date().toISOString().split('T')[0];
    this.finTemp[id] = t?.dateFinAbonnement ?? new Date(Date.now() + 365 * 86400000).toISOString().split('T')[0];
    this.montantTemp[id] = t?.montantAbonnement ?? 0;
  }

  fermerModifierPlan(id: number): void {
    this.showPlanModal[id] = false;
  }

  confirmerModifierPlan(id: number): void {
    const plan = this.planTemp[id];
    const debut = this.debutTemp[id];
    const fin = this.finTemp[id];
    const montant = this.montantTemp[id];
    if (!plan || !debut || !fin || !montant) return;
    this.tenantService.modifierPlanAbonnement(id, plan, debut, fin, montant).subscribe({
      next: (t) => {
        this.mettreAJourTenant(t);
        this.showPlanModal[id] = false;
        this.toast('Plan d\'abonnement modifié');
      },
      error: () => {
        const t = this.trouverTenant(id);
        if (t) {
          t.planAbonnement = plan;
          t.dateDebutAbonnement = debut;
          t.dateFinAbonnement = fin;
          t.montantAbonnement = montant;
          this.tenants = [...this.tenants];
        }
        this.showPlanModal[id] = false;
        this.toast('Plan modifié (mode démo)');
      },
    });
  }

  private trouverTenant(id: number): Tenant | undefined {
    const list = this.tenants.length > 0 ? this.tenants : this.tenantsMock;
    return list.find((t) => t.id === id);
  }

  private mettreAJourTenant(t: Tenant): void {
    const list = this.tenants.length > 0 ? this.tenants : this.tenantsMock;
    const idx = list.findIndex((x) => x.id === t.id);
    if (idx >= 0) { list[idx] = t; } else { list.unshift(t); }
    this.tenants = [...list];
  }

  toast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  getListLength(): number {
    return this.tenants.length || this.tenantsMock.length;
  }
}
