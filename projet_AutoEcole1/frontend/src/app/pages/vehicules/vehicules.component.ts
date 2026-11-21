import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { VehiculeService } from '../../core/services/vehicule.service';
import { UtilisateurService } from '../../core/services/utilisateur.service';
import { AuthService } from '../../core/services/auth.service';
import { VehiculeDTO, VehiculeAlerteDTO, CreateVehiculeRequest } from '../../core/models/vehicule';
import { UserDTO } from '../../core/models/user';

@Component({
  selector: 'app-vehicules',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './vehicules.component.html',
})
export class VehiculesComponent implements OnInit {
  vehicules: VehiculeDTO[] = [];
  vehiculesMock: VehiculeDTO[] = [
    { id: 1, tenantId: 1, immatriculation: '12345-A-12', categoriePermis: 'B', dateMiseService: '2020-03-15', dateControleTechnique: '2026-05-20', dateExpirationAssurance: '2026-04-10', moniteurId: 1, moniteurNom: 'El Amrani', moniteurPrenom: 'Ahmed' },
    { id: 2, tenantId: 1, immatriculation: '67890-B-34', categoriePermis: 'B', dateMiseService: '2021-06-01', dateControleTechnique: '2026-09-15', dateExpirationAssurance: '2025-08-22', moniteurId: null, moniteurNom: null, moniteurPrenom: null },
    { id: 3, tenantId: 1, immatriculation: '11223-C-56', categoriePermis: 'C', dateMiseService: '2019-11-10', dateControleTechnique: '2025-07-05', dateExpirationAssurance: '2026-02-28', moniteurId: 2, moniteurNom: 'Bennani', moniteurPrenom: 'Karim' },
  ];
  alertes: VehiculeAlerteDTO[] = [];
  moniteurs: UserDTO[] = [];
  loading = false;
  showForm = false;
  searchTerm = '';
  selectedVehiculeId: number | null = null;
  selectedMoniteurId: number | null = null;
  showAttribForm = false;
  successMessage = '';

  form: CreateVehiculeRequest = {
    immatriculation: '',
    categoriePermis: 'B',
    dateMiseService: null,
    dateControleTechnique: null,
    dateExpirationAssurance: null,
    moniteurId: null,
  };

  categoriesPermis = ['B', 'C', 'D', 'EB', 'EC', 'ED', 'A', 'A1', 'A2'];

  constructor(
    private vehiculeService: VehiculeService,
    private utilisateurService: UtilisateurService,
    private authService: AuthService
  ) {}

  canWriteVehicules(): boolean { return this.authService.canWrite('vehicules'); }
  isMoniteurRole(): boolean { return this.authService.isMoniteur(); }

  ngOnInit(): void {
    this.loadVehicules();
    if (this.canWriteVehicules()) {
      this.loadAlertes();
      this.loadMoniteurs();
    }
  }

  loadVehicules(): void {
    this.loading = true;
    this.vehiculeService.lister().subscribe({
      next: (data) => {
        this.vehicules = data;
        this.loading = false;
      },
      error: () => {
        this.vehicules = this.vehiculesMock;
        this.loading = false;
      },
    });
  }

  loadAlertes(): void {
    this.vehiculeService.getAlertes().subscribe({
      next: (data) => (this.alertes = data),
      error: () => (this.alertes = this.generateMockAlertes()),
    });
  }

  loadMoniteurs(): void {
    this.utilisateurService.lister().subscribe({
      next: (data) => (this.moniteurs = data.filter((u) => u.role === 'MONITEUR')),
      error: () => (this.moniteurs = []),
    });
  }

  generateMockAlertes(): VehiculeAlerteDTO[] {
    return [
      { vehiculeId: 2, immatriculation: '67890-B-34', typeAlerte: 'ASSURANCE', libelle: 'Expiration assurance', dateEvenement: '2025-08-22', joursRestants: -10, niveau: 'CRITIQUE' },
      { vehiculeId: 3, immatriculation: '11223-C-56', typeAlerte: 'CONTROLE_TECHNIQUE', libelle: 'Controle technique', dateEvenement: '2025-07-05', joursRestants: 5, niveau: 'URGENT' },
    ];
  }

  get vehiculesFiltres(): VehiculeDTO[] {
    const list = this.vehicules.length > 0 ? this.vehicules : this.vehiculesMock;
    if (!this.searchTerm) return list;
    const q = this.searchTerm.toLowerCase();
    return list.filter(
      (v) =>
        v.immatriculation.toLowerCase().includes(q) ||
        v.categoriePermis.toLowerCase().includes(q) ||
        ((v.moniteurNom + ' ' + v.moniteurPrenom).toLowerCase().includes(q))
    );
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

  getAlerteNiveauClass(niveau: string): string {
    switch (niveau) {
      case 'CRITIQUE': return 'status-stop';
      case 'URGENT': return 'status-warn';
      default: return 'status-warn';
    }
  }

  formatDate(d?: string | null): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR');
  }

  joursRestantsTexte(jours?: number | null): string {
    if (jours === null || jours === undefined) return '';
    if (jours < 0) return `${-jours}j de retard`;
    if (jours === 0) return "Aujourd'hui";
    return `${jours}j restants`;
  }

  ouvrirForm(): void {
    this.showForm = true;
    this.form = { immatriculation: '', categoriePermis: 'B', dateMiseService: null, dateControleTechnique: null, dateExpirationAssurance: null, moniteurId: null };
  }

  fermerForm(): void {
    this.showForm = false;
  }

  soumettreForm(): void {
    if (!this.form.immatriculation) return;
    this.vehiculeService.creer(this.form).subscribe({
      next: (v) => {
        this.vehicules.push(v);
        this.showForm = false;
        this.successMessage = 'Véhicule ajouté avec succès';
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (e) => {
        this.vehiculesMock.push({ ...this.form, id: Date.now(), tenantId: 1, moniteurNom: null, moniteurPrenom: null } as any);
        this.vehicules = [...this.vehicules];
        this.showForm = false;
      },
    });
  }

  ouvrirAttrib(vid: number): void {
    this.selectedVehiculeId = vid;
    const v = this.vehiculesFiltres.find((x) => x.id === vid);
    this.selectedMoniteurId = v?.moniteurId ?? null;
    this.showAttribForm = true;
  }

  fermerAttrib(): void {
    this.showAttribForm = false;
    this.selectedVehiculeId = null;
  }

  confirmerAttrib(): void {
    if (this.selectedVehiculeId == null) return;
    this.vehiculeService.attribuerMoniteur(this.selectedVehiculeId, this.selectedMoniteurId).subscribe({
      next: (v) => {
        const idx = this.vehicules.findIndex((x) => x.id === v.id);
        if (idx >= 0) this.vehicules[idx] = v;
        this.showAttribForm = false;
        this.successMessage = 'Moniteur attribué';
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: () => {
        this.showAttribForm = false;
      },
    });
  }

  getMoniteurNomComplet(v: VehiculeDTO): string {
    if (v.moniteurNom || v.moniteurPrenom) {
      return `${v.moniteurPrenom ?? ''} ${v.moniteurNom ?? ''}`.trim();
    }
    return '— Non attribué';
  }

  getMoniteurAttribClass(v: VehiculeDTO): string {
    return v.moniteurId ? 'text-secondary' : 'text-stop';
  }
}
