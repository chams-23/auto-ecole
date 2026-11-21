import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';

import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CandidatsComponent } from './pages/candidats/candidats.component';
import { CandidatFormComponent } from './pages/candidats/candidat-form.component';
import { CandidatDetailComponent } from './pages/candidats/candidat-detail.component';
import { PlanningComponent } from './pages/planning/planning.component';
import { VehiculesComponent } from './pages/vehicules/vehicules.component';
import { TresorerieComponent } from './pages/tresorerie/tresorerie.component';
import { QuotasComponent } from './pages/quotas/quotas.component';
import { LandingComponent } from './auth/landing/landing.component';
import { SignInComponent } from './auth/signin/signin.component';
import { InscriptionComponent } from './auth/inscription/inscription.component';
import { UtilisateursComponent } from './pages/utilisateurs/utilisateurs.component';
import { NotificationsComponent } from './pages/notifications/notifications.component';
import { TenantsComponent } from './pages/tenants/tenants.component';
import { AuditComponent } from './pages/audit/audit.component';

import { AuthGuard } from './core/auth/auth.guard';
import { rbacGuard } from './core/auth/rbac-guard';
import { AuthInterceptor } from './core/services/http-interceptor';

const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'signin', component: SignInComponent },
  { path: 'login', redirectTo: 'signin', pathMatch: 'full' },
  { path: 'inscription', component: InscriptionComponent },
  { path: 'register', redirectTo: 'inscription', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'candidats', component: CandidatsComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE', 'MONITEUR')] },
  { path: 'candidats/nouveau', component: CandidatFormComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE')] },
  { path: 'candidats/:id', component: CandidatDetailComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE', 'MONITEUR', 'CANDIDAT')] },
  { path: 'candidats/:id/modifier', component: CandidatFormComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE')] },
  { path: 'planning', component: PlanningComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE', 'MONITEUR', 'CANDIDAT')] },
  { path: 'vehicules', component: VehiculesComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'MONITEUR')] },
  { path: 'tresorerie', component: TresorerieComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN', 'SECRETAIRE', 'CANDIDAT')] },
  { path: 'quotas', component: QuotasComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN')] },
  { path: 'utilisateurs', component: UtilisateursComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN')] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  { path: 'tenants', component: TenantsComponent, canActivate: [rbacGuard('SUPER_ADMIN')] },
  { path: 'audit', component: AuditComponent, canActivate: [rbacGuard('ADMIN', 'SUPER_ADMIN')] },
  { path: '**', redirectTo: '' },
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
};
