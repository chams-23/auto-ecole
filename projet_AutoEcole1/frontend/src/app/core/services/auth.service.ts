import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, of } from 'rxjs';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { UserDTO, LoginResponse } from '../models/user';

export type DemoRole = 'SUPER_ADMIN' | 'ADMIN' | 'SECRETAIRE' | 'MONITEUR' | 'CANDIDAT';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = environment.apiUrl;
  private TOKEN_KEY = 'auth_token';
  private USER_KEY = 'auth_user';
  private DEMO_FLAG_KEY = 'auth_demo';

  private DEMO_USERS: Record<DemoRole, Omit<UserDTO, 'id'> & { id: number }> = {
    SUPER_ADMIN: { id: 1, tenantId: 1, role: 'SUPER_ADMIN', nom: 'Admin', prenom: 'System', email: 'admin@autoecole.ma', telephone: '0522000000', statut: 'ACTIF' },
    ADMIN:       { id: 2, tenantId: 1, role: 'ADMIN', nom: 'Zahi', prenom: 'Fatima', email: 'fatima@autoecole.ma', telephone: '0600000001', statut: 'ACTIF' },
    SECRETAIRE:  { id: 5, tenantId: 1, role: 'SECRETAIRE', nom: 'Idrissi', prenom: 'Salma', email: 'salma@autoecole.ma', telephone: '0600000004', statut: 'ACTIF' },
    MONITEUR:    { id: 3, tenantId: 1, role: 'MONITEUR', nom: 'Amrani', prenom: 'Youssef', email: 'youssef@autoecole.ma', telephone: '0600000002', statut: 'ACTIF' },
    CANDIDAT:    { id: 6, tenantId: 1, role: 'CANDIDAT', nom: 'Tazi', prenom: 'Karim', email: 'karim@email.ma', telephone: '0611111111', statut: 'ACTIF' }
  };

  constructor(private http: HttpClient, private router: Router) {}

  private buildDemoJwt(role: DemoRole): string {
    const header = btoa(JSON.stringify({ alg: 'none', typ: 'JWT' }));
    const payload = {
      sub: this.DEMO_USERS[role].email,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + 365 * 24 * 3600,
      authorities: [role]
    };
    const payloadB64 = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    const headerB64 = header.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    return `${headerB64}.${payloadB64}.demo-signature`;
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/auth/login`, { email, password })
      .pipe(
        tap((response) => {
          if (response.token && response.user) {
            localStorage.removeItem(this.DEMO_FLAG_KEY);
            this.setToken(response.token);
            this.setCurrentUser(response.user);
          }
        })
      );
  }

  /** Inscription publique élève — crée le compte et connecte automatiquement. */
  inscription(payload: {
    nom: string;
    prenom: string;
    email: string;
    telephone?: string | null;
    password: string;
    cin: string;
    categoriePermis: string;
    forfaitId?: number | null;
  }): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/auth/inscription`, payload)
      .pipe(
        tap((response) => {
          if (response.token && response.user) {
            localStorage.removeItem(this.DEMO_FLAG_KEY);
            this.setToken(response.token);
            this.setCurrentUser(response.user);
          }
        })
      );
  }

  loginDemo(role: DemoRole = 'ADMIN'): Observable<LoginResponse> {
    const user: UserDTO = { ...this.DEMO_USERS[role] };
    const token = this.buildDemoJwt(role);
    const response: LoginResponse = { token, user };
    localStorage.setItem(this.DEMO_FLAG_KEY, 'true');
    this.setToken(token);
    this.setCurrentUser(user);
    return of(response);
  }

  isDemoMode(): boolean {
    return localStorage.getItem(this.DEMO_FLAG_KEY) === 'true';
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.DEMO_FLAG_KEY);
    this.router.navigate(['/signin']);
  }

  isAuthenticated(): boolean {
    if (this.isDemoMode()) {
      const user = this.getCurrentUser();
      return !!user;
    }
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeJwtPayload(token);
      if (!payload || !payload.exp) return false;
      return payload.exp > Date.now() / 1000;
    } catch {
      return false;
    }
  }

  isLoggedIn(): boolean {
    return this.isAuthenticated();
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): UserDTO | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr) as UserDTO;
    } catch {
      return null;
    }
  }

  getCurrentUserId(): number | null {
    const u = this.getCurrentUser();
    return u?.id ?? null;
  }

  getCurrentUserEmail(): string | null {
    const u = this.getCurrentUser();
    return u?.email ?? null;
  }

  getCurrentRole(): DemoRole | null {
    const user = this.getCurrentUser();
    if (!user) return null;
    return user.role as DemoRole;
  }

  hasRole(...roles: DemoRole[]): boolean {
    const current = this.getCurrentRole();
    return !!current && roles.includes(current);
  }

  isAdmin(): boolean { return this.hasRole('ADMIN', 'SUPER_ADMIN'); }
  isMoniteur(): boolean { return this.hasRole('MONITEUR'); }
  isCandidat(): boolean { return this.hasRole('CANDIDAT'); }
  isSecretaire(): boolean { return this.hasRole('SECRETAIRE'); }

  /** Navigation — matrice CDC §3.1 */
  canAccess(ecran: string): boolean {
    switch (ecran) {
      case 'dashboard':
        return true;
      case 'candidats':
        return this.isAdmin() || this.isSecretaire() || this.isMoniteur();
      case 'vehicules':
        return this.isAdmin() || this.isMoniteur();
      case 'planning':
        return this.isAdmin() || this.isSecretaire() || this.isMoniteur() || this.isCandidat();
      case 'tresorerie':
        return this.isAdmin() || this.isSecretaire() || this.isCandidat();
      case 'utilisateurs':
        return this.isAdmin();
      case 'tarifs':
        return this.isAdmin();
      case 'notifications':
        return this.isLoggedIn();
      case 'audit':
        return this.isAdmin();
      case 'tenants':
        return this.hasRole('SUPER_ADMIN');
      case 'quotas':
        return this.isAdmin();
      default:
        return this.isAdmin();
    }
  }

  /** Écriture — matrice CDC §3.1 */
  canWrite(ecran: string): boolean {
    switch (ecran) {
      case 'candidats':
        return this.isAdmin() || this.isSecretaire();
      case 'vehicules':
        return this.isAdmin();
      case 'planning':
        return this.isAdmin() || this.isSecretaire();
      case 'tresorerie':
        return this.isAdmin() || this.isSecretaire();
      case 'utilisateurs':
        return this.isAdmin();
      case 'tarifs':
        return this.isAdmin();
      case 'quotas':
        return this.isAdmin();
      case 'tenants':
        return this.hasRole('SUPER_ADMIN');
      default:
        return this.isAdmin();
    }
  }

  /** Actions fines de la matrice */
  canInscrireCandidat(): boolean { return this.isAdmin() || this.isSecretaire(); }
  canSaisirPaiement(): boolean { return this.isAdmin() || this.isSecretaire(); }
  canSaisirCharge(): boolean { return this.isAdmin() || this.isSecretaire(); }
  canAttribuerVehicule(): boolean { return this.isAdmin(); }
  canValiderConduiteAsMoniteur(): boolean { return this.isMoniteur(); }
  canValiderTheorieAsSecretaire(): boolean { return this.isSecretaire(); }
  canConfirmerSeanceAsCandidat(): boolean { return this.isCandidat(); }
  canVoirSolde(): boolean { return this.isAdmin() || this.isSecretaire() || this.isCandidat(); }
  canRecevoirNotifications(): boolean {
    return this.isSecretaire() || this.isMoniteur() || this.isCandidat() || this.isAdmin();
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setCurrentUser(user: UserDTO): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private decodeJwtPayload(token: string): any {
    const parts = token.split('.');
    if (parts.length !== 3) throw new Error('JWT invalide');
    let payload = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/');
    switch (payload.length % 4) {
      case 0:
        break;
      case 2:
        payload += '==';
        break;
      case 3:
        payload += '=';
        break;
      default:
        throw new Error('Payload JWT invalide');
    }
    return JSON.parse(atob(payload));
  }
}
