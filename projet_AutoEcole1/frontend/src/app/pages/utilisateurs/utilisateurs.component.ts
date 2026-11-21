import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { UtilisateurService } from '../../core/services/utilisateur.service';
import { AuthService } from '../../core/services/auth.service';
import { UtilisateurDTO, CreateUtilisateurRequest } from '../../core/models/user';

type Role = 'SUPER_ADMIN' | 'ADMIN' | 'SECRETAIRE' | 'MONITEUR' | 'CANDIDAT';
type Statut = 'ACTIF' | 'DESACTIVE';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './utilisateurs.component.html',
})
export class UtilisateursComponent implements OnInit {
  utilisateurs: UtilisateurDTO[] = [];
  searchTerm = '';
  loading = false;
  showForm = false;
  modeForm: 'CREER' | 'MODIFIER' = 'CREER';
  selectedId: number | null = null;
  successMessage = '';

  utilisateursMock: any[] = [
    { id: 1, tenantId: 1, role: 'SUPER_ADMIN', nom: 'Admin', prenom: 'System', email: 'admin@autoecole.ma', telephone: '0522000000', statut: 'ACTIF' },
    { id: 2, tenantId: 1, role: 'ADMIN', nom: 'Zahi', prenom: 'Fatima', email: 'fatima@autoecole.ma', telephone: '0600000001', statut: 'ACTIF' },
    { id: 3, tenantId: 1, role: 'MONITEUR', nom: 'Amrani', prenom: 'Youssef', email: 'youssef@autoecole.ma', telephone: '0600000002', statut: 'ACTIF' },
    { id: 4, tenantId: 1, role: 'MONITEUR', nom: 'Bennani', prenom: 'Hassan', email: 'hassan@autoecole.ma', telephone: '0600000003', statut: 'ACTIF' },
    { id: 5, tenantId: 1, role: 'SECRETAIRE', nom: 'Idrissi', prenom: 'Salma', email: 'salma@autoecole.ma', telephone: '0600000004', statut: 'ACTIF' },
    { id: 6, tenantId: 1, role: 'CANDIDAT', nom: 'Tazi', prenom: 'Karim', email: 'karim@email.ma', telephone: '0611111111', statut: 'DESACTIVE' },
  ];

  rolesDisponibles: { v: Role; l: string }[] = [
    { v: 'ADMIN', l: 'Admin' },
    { v: 'SECRETAIRE', l: 'Secrétaire' },
    { v: 'MONITEUR', l: 'Moniteur' },
    { v: 'CANDIDAT', l: 'Candidat' },
  ];

  formUtilisateur: any = {
    role: 'CANDIDAT' as Role,
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    password: '',
  };

  constructor(
    private utilisateurService: UtilisateurService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadUtilisateurs();
  }

  canWrite(): boolean { return this.authService.canWrite('utilisateurs'); }
  isAdmin(): boolean { return this.authService.isAdmin(); }

  loadUtilisateurs(): void {
    this.loading = true;
    this.utilisateurService.lister().subscribe({
      next: (data) => {
        this.utilisateurs = data;
        this.loading = false;
      },
      error: () => {
        this.utilisateurs = [...this.utilisateursMock];
        this.loading = false;
      }
    });
  }

  get utilisateursFiltres(): any[] {
    const list = this.utilisateurs.length > 0 ? this.utilisateurs : [...this.utilisateursMock];
    if (!this.searchTerm) return list;
    const q = this.searchTerm.toLowerCase();
    return list.filter((u: any) =>
      (u.nom + ' ' + u.prenom).toLowerCase().includes(q) ||
      (u.email || '').toLowerCase().includes(q) ||
      (u.telephone || '').toLowerCase().includes(q)
    );
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN':
      case 'SUPER_ADMIN': return 'badge-blue';
      case 'SECRETAIRE': return 'badge-purple';
      case 'MONITEUR': return 'badge-green';
      case 'CANDIDAT': return 'badge-orange';
      default: return 'badge-gray';
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'SUPER_ADMIN': return 'Super Admin';
      case 'ADMIN': return 'Admin';
      case 'SECRETAIRE': return 'Secrétaire';
      case 'MONITEUR': return 'Moniteur';
      case 'CANDIDAT': return 'Candidat';
      default: return role;
    }
  }

  getStatutDisplay(statut: string): { class: string; text: string } {
    switch (statut) {
      case 'ACTIF':
        return { class: 'status-go', text: '✓ ACTIF' };
      case 'DESACTIVE':
        return { class: 'status-stop', text: '✕ DÉSACTIVÉ' };
      default:
        return { class: 'status-warn', text: statut };
    }
  }

  ouvrirCreer(): void {
    if (!this.canWrite()) return;
    this.modeForm = 'CREER';
    this.selectedId = null;
    this.showForm = true;
    this.formUtilisateur = {
      role: 'CANDIDAT' as Role,
      nom: '',
      prenom: '',
      email: '',
      telephone: '',
      password: '',
    };
  }

  ouvrirModifier(u: UtilisateurDTO): void {
    if (!this.canWrite()) return;
    this.modeForm = 'MODIFIER';
    this.selectedId = u.id;
    this.showForm = true;
    this.formUtilisateur = {
      role: u.role as Role,
      nom: u.nom,
      prenom: u.prenom,
      email: u.email,
      telephone: u.telephone,
      password: '',
    };
  }

  fermerForm(): void {
    this.showForm = false;
    this.selectedId = null;
  }

  formEstValide(): boolean {
    const f = this.formUtilisateur;
    if (!f.role || !f.nom?.trim() || !f.prenom?.trim() || !f.email?.trim()) return false;
    if (this.modeForm === 'CREER' && !f.password?.trim()) return false;
    return true;
  }

  soumettreForm(): void {
    if (!this.formEstValide()) return;

    if (this.modeForm === 'CREER') {
      const req: CreateUtilisateurRequest = {
        role: this.formUtilisateur.role,
        nom: this.formUtilisateur.nom.trim(),
        prenom: this.formUtilisateur.prenom.trim(),
        email: this.formUtilisateur.email.trim(),
        telephone: this.formUtilisateur.telephone?.trim() || '',
        password: this.formUtilisateur.password,
      };
      this.utilisateurService.creer(req).subscribe({
        next: (u) => {
          this.utilisateurs.unshift(u);
          this.showForm = false;
          this.toast('Utilisateur créé avec succès');
          this.loadUtilisateurs();
        },
        error: () => {
          const nouveau = {
            id: Date.now(),
            tenantId: 1,
            role: req.role,
            nom: req.nom,
            prenom: req.prenom,
            email: req.email,
            telephone: req.telephone,
            statut: 'ACTIF' as Statut,
          };
          this.utilisateursMock.unshift(nouveau);
          this.utilisateurs = [...this.utilisateurs];
          this.showForm = false;
          this.toast('Utilisateur créé (mode démo)');
        },
      });
    } else {
      const id = this.selectedId!;
      const dto: UtilisateurDTO = {
        id,
        tenantId: this.utilisateurs.find(u => u.id === id)?.tenantId ?? 1,
        role: this.formUtilisateur.role,
        nom: this.formUtilisateur.nom.trim(),
        prenom: this.formUtilisateur.prenom.trim(),
        email: this.formUtilisateur.email.trim(),
        telephone: this.formUtilisateur.telephone?.trim() || '',
        statut: this.utilisateurs.find(u => u.id === id)?.statut ?? 'ACTIF',
      };
      this.utilisateurService.modifier(id, dto).subscribe({
        next: (u) => {
          const idx = this.utilisateurs.findIndex(x => x.id === id);
          if (idx >= 0) this.utilisateurs[idx] = u;
          this.showForm = false;
          this.toast('Utilisateur modifié avec succès');
        },
        error: () => {
          const idx = this.utilisateurs.findIndex(x => x.id === id);
          if (idx >= 0) this.utilisateurs[idx] = dto;
          const idxMock = this.utilisateursMock.findIndex(x => x.id === id);
          if (idxMock >= 0) this.utilisateursMock[idxMock] = dto;
          this.showForm = false;
          this.toast('Utilisateur modifié (mode démo)');
        },
      });
    }
  }

  desactiver(u: UtilisateurDTO): void {
    if (!this.isAdmin()) return;
    if (!confirm(`Désactiver l'utilisateur ${u.prenom} ${u.nom} ?`)) return;
    this.utilisateurService.desactiver(u.id).subscribe({
      next: () => {
        const idx = this.utilisateurs.findIndex(x => x.id === u.id);
        if (idx >= 0) this.utilisateurs[idx] = { ...this.utilisateurs[idx], statut: 'DESACTIVE' };
        this.toast('Utilisateur désactivé');
      },
      error: () => {
        const idx = this.utilisateurs.findIndex(x => x.id === u.id);
        if (idx >= 0) this.utilisateurs[idx] = { ...this.utilisateurs[idx], statut: 'DESACTIVE' };
        const idxMock = this.utilisateursMock.findIndex(x => x.id === u.id);
        if (idxMock >= 0) this.utilisateursMock[idxMock] = { ...this.utilisateursMock[idxMock], statut: 'DESACTIVE' };
        this.toast('Utilisateur désactivé (mode démo)');
      },
    });
  }

  toast(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => (this.successMessage = ''), 3000);
  }

  getUtilisateursLength(): number {
    return this.utilisateurs.length || this.utilisateursMock.length;
  }

  peutDesactiver(u: UtilisateurDTO): boolean {
    return this.isAdmin() && u.statut === 'ACTIF';
  }
}
