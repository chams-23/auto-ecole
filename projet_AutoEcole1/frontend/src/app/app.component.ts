import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router, NavigationEnd } from '@angular/router';

import { AuthService } from './core/services/auth.service';
import { NotificationService } from './core/services/notification.service';
import { NotificationDTO } from './core/models/notification';
import { UserDTO } from './core/models/user';

interface NavItem {
  label: string;
  route: string;
  ecran: string;
  exact?: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);

  isLoginPage = false;
  showNotifDropdown = false;
  showUserMenu = false;
  notifications: NotificationDTO[] = [];
  nonLuesCount = 0;
  currentUser?: UserDTO | null;
  private refreshInterval: any = null;

  private mockNotifications: NotificationDTO[] = [
    { id: 1, destinataireId: 1, typeEvenement: 'SEANCE_VALIDEE', contenu: 'Séance de conduite validée par Youssef Amrani', date: new Date().toISOString(), statutLecture: false },
    { id: 2, destinataireId: 1, typeEvenement: 'PAIEMENT_CREE', contenu: 'Nouveau paiement de 1 500 DH reçu de Sara Benali', date: new Date(Date.now() - 3600000).toISOString(), statutLecture: false },
    { id: 3, destinataireId: 1, typeEvenement: 'NOUVELLE_SEANCE', contenu: 'Nouvelle séance programmée le 25/08 à 10h', date: new Date(Date.now() - 86400000).toISOString(), statutLecture: true },
    { id: 4, destinataireId: 1, typeEvenement: 'ALERTE_VEHICULE', contenu: 'Véhicule « 123-A-12 » : contrôle technique dans 7 jours', date: new Date(Date.now() - 2 * 86400000).toISOString(), statutLecture: true },
  ];

  private allNavItems: NavItem[] = [
    { label: 'Tableau de bord', route: '/dashboard', ecran: 'dashboard', exact: true },
    { label: 'Candidats', route: '/candidats', ecran: 'candidats' },
    { label: 'Planning', route: '/planning', ecran: 'planning' },
    { label: 'Véhicules', route: '/vehicules', ecran: 'vehicules' },
    { label: 'Trésorerie', route: '/tresorerie', ecran: 'tresorerie' },
    { label: 'Quotas CNEP', route: '/quotas', ecran: 'quotas' },
    { label: 'Utilisateurs', route: '/utilisateurs', ecran: 'utilisateurs' },
    { label: 'Notifications', route: '/notifications', ecran: 'notifications' },
    { label: 'Audit', route: '/audit', ecran: 'audit' },
    { label: 'Auto-écoles', route: '/tenants', ecran: 'tenants' }
  ];

  get navItems(): NavItem[] {
    return this.allNavItems.filter(item => this.authService.canAccess(item.ecran));
  }

  ngOnInit(): void {
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd) {
        this.isLoginPage = e.urlAfterRedirects.startsWith('/signin')
          || e.urlAfterRedirects.startsWith('/login')
          || e.urlAfterRedirects.startsWith('/inscription')
          || e.urlAfterRedirects.startsWith('/register')
          || e.urlAfterRedirects === '/'
          || e.url === '/'
          || e.url.startsWith('/signin')
          || e.url.startsWith('/login')
          || e.url.startsWith('/inscription')
          || e.url.startsWith('/register');
        this.currentUser = this.authService.getCurrentUser();
        if (!this.isLoginPage) {
          this.loadNotifications();
          this.startAutoRefresh();
        } else {
          this.stopAutoRefresh();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.stopAutoRefresh();
  }

  private startAutoRefresh(): void {
    this.stopAutoRefresh();
    this.refreshInterval = setInterval(() => {
      if (!this.showNotifDropdown) {
        this.loadNotifications(true);
      }
    }, 30000);
  }

  private stopAutoRefresh(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
      this.refreshInterval = null;
    }
  }

  isDemoMode(): boolean {
    return this.authService.isDemoMode();
  }

  roleLabel(): string {
    const role = this.authService.getCurrentRole();
    switch (role) {
      case 'ADMIN': case 'SUPER_ADMIN': return 'Administrateur';
      case 'SECRETAIRE': return 'Secrétaire';
      case 'MONITEUR': return 'Moniteur';
      case 'CANDIDAT': return 'Élève';
      default: return role ?? '';
    }
  }

  loadNotifications(silent = false): void {
    if (!this.authService.isLoggedIn()) {
      this.currentUser = null;
      this.notifications = [];
      this.nonLuesCount = 0;
      return;
    }
    this.currentUser = this.authService.getCurrentUser();

    if (this.authService.isDemoMode()) {
      this.applyNotifications(this.mockNotifications);
      return;
    }

    this.notificationService.lister().subscribe({
      next: (notifs) => {
        this.applyNotifications(notifs);
      },
      error: () => {
        if (!silent) {
          this.applyNotifications(this.mockNotifications);
        }
      }
    });
  }

  private applyNotifications(notifs: NotificationDTO[]): void {
    this.notifications = notifs.slice(0, 10);
    this.nonLuesCount = notifs.filter(n => !n.statutLecture).length;
  }

  toggleNotifDropdown(): void {
    this.showUserMenu = false;
    this.showNotifDropdown = !this.showNotifDropdown;
    if (this.showNotifDropdown) {
      this.loadNotifications(true);
    }
  }

  toggleUserMenu(): void {
    this.showNotifDropdown = false;
    this.showUserMenu = !this.showUserMenu;
  }

  closeDropdowns(): void {
    this.showNotifDropdown = false;
    this.showUserMenu = false;
  }

  marquerLue(id: number): void {
    const n = this.notifications.find(x => x.id === id);
    if (!n || n.statutLecture) return;

    if (this.authService.isDemoMode()) {
      n.statutLecture = true;
      this.nonLuesCount = Math.max(0, this.nonLuesCount - 1);
      return;
    }

    this.notificationService.marquerLue(id).subscribe({
      next: () => {
        n.statutLecture = true;
        this.nonLuesCount = Math.max(0, this.nonLuesCount - 1);
      },
      error: () => {}
    });
  }

  onLogout(): void {
    this.stopAutoRefresh();
    this.authService.logout();
  }

  getTypeIcone(type: string): string {
    switch (type) {
      case 'SEANCE_VALIDEE':
      case 'CANDIDAT_ADMIS':
      case 'PAIEMENT_CREE':
        return '✓';
      case 'SEANCE_CONTESTEE':
      case 'ALERTE_VEHICULE':
      case 'PAIEMENT_EN_RETARD':
        return '⚠';
      case 'NOUVELLE_SEANCE':
      case 'SEANCE_REPROGRAMMEE':
      case 'SEANCE_DECLAREE':
        return '→';
      default:
        return '•';
    }
  }

  getTypeClass(type: string): string {
    switch (type) {
      case 'SEANCE_VALIDEE':
      case 'CANDIDAT_ADMIS':
      case 'PAIEMENT_CREE':
        return 'notif-go';
      case 'SEANCE_CONTESTEE':
      case 'ALERTE_VEHICULE':
      case 'PAIEMENT_EN_RETARD':
        return 'notif-stop';
      default:
        return 'notif-warn';
    }
  }

  formatDate(d: string): string {
    if (!d) return '';
    try {
      const date = new Date(d);
      return date.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
    } catch {
      return d;
    }
  }
}
