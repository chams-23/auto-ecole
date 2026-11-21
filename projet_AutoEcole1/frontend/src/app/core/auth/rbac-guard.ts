import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export function rbacGuard(...allowedRoles: string[]): CanActivateFn {
  return (): boolean | UrlTree => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/signin']);
    }

    if (allowedRoles.length === 0) {
      return true;
    }

    const hasRole = allowedRoles.some(r => authService.hasRole(r as any));
    if (!hasRole) {
      return router.createUrlTree(['/dashboard']);
    }

    return true;
  };
}
