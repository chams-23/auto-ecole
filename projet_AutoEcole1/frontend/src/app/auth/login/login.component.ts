import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService, DemoRole } from '../../core/services/auth.service';

interface DemoPreset {
  role: DemoRole;
  label: string;
  description: string;
  icon: string;
  accent: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage: string | null = null;

  presets: DemoPreset[] = [
    { role: 'ADMIN', label: 'Administrateur', description: 'Accès complet', icon: 'Ad', accent: 'go' },
    { role: 'SECRETAIRE', label: 'Secrétaire', description: 'Candidats & paiements', icon: 'Se', accent: 'warn' },
    { role: 'MONITEUR', label: 'Moniteur', description: 'Séances & véhicules', icon: 'Mo', accent: 'silver' },
    { role: 'CANDIDAT', label: 'Candidat', description: 'Vue élève', icon: 'Él', accent: 'blue' }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['admin@autoecole.ma', [Validators.required, Validators.email]],
      password: ['password123', [Validators.required, Validators.minLength(6)]]
    });
  }

  showDemo = false;

  accounts = [
    { role: 'Administrateur', email: 'admin@autoecole.ma' },
    { role: 'Secrétaire',     email: 'salma@autoecole.ma' },
    { role: 'Moniteur',       email: 'youssef@autoecole.ma' },
    { role: 'Élève',          email: 'karim@email.ma' },
  ];

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMessage = null;

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.errorMessage = 'Identifiants incorrects. Vérifiez votre email et votre mot de passe.';
        } else if (err.status === 0 || err.status >= 500) {
          this.errorMessage = 'Serveur indisponible — utilisez un accès rapide ci-dessous.';
        } else {
          this.errorMessage = 'Erreur de connexion. Veuillez réessayer ultérieurement.';
        }
      }
    });
  }

  connexionRapide(role: DemoRole): void {
    this.loading = true;
    this.errorMessage = null;
    this.authService.loginDemo(role).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Erreur de connexion rapide';
      }
    });
  }
}
