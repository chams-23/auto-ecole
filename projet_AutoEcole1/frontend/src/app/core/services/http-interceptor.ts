import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    let headers = request.headers;

    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    const currentUser = this.authService.getCurrentUser();
    if (currentUser?.tenantId != null) {
      headers = headers.set('X-Tenant-Id', String(currentUser.tenantId));
    }

    const cloned = request.clone({ headers });

    return next.handle(cloned).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !this.authService.isDemoMode()) {
          this.authService.logout();
          this.router.navigate(['/signin']);
        }
        return throwError(() => error);
      })
    );
  }
}
