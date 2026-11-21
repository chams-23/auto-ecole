import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { AuthService, DemoRole } from '../../core/services/auth.service';

interface DemoPreset {
  role: DemoRole;
  label: string;
  description: string;
  icon: string;
  accent: string;
}

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './signin.component.html',
})
export class SignInComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  signInForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  loading = false;
  errorMessage: string | null = null;
  showDemo = false;

  presets: DemoPreset[] = [
    { role: 'ADMIN', label: 'Administrateur', description: 'Accès complet', icon: 'Ad', accent: 'go' },
    { role: 'SECRETAIRE', label: 'Secrétaire', description: 'Candidats & paiements', icon: 'Se', accent: 'warn' },
    { role: 'MONITEUR', label: 'Moniteur', description: 'Séances & véhicules', icon: 'Mo', accent: 'silver' },
    { role: 'CANDIDAT', label: 'Candidat', description: 'Vue élève', icon: 'Él', accent: 'blue' },
  ];

  accounts = [
    { role: 'Administrateur', email: 'admin@autoecole.ma' },
    { role: 'Secrétaire', email: 'salma@autoecole.ma' },
    { role: 'Moniteur', email: 'youssef@autoecole.ma' },
    { role: 'Élève', email: 'karim@email.ma' },
  ];

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.route.queryParamMap.subscribe((params) => {
      if (params.get('demo') === '1') {
        this.showDemo = true;
      }
    });
  }

  onSubmit(): void {
    if (this.signInForm.invalid) return;

    this.loading = true;
    this.errorMessage = null;
    const { email, password } = this.signInForm.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.errorMessage = 'Identifiants incorrects. Vérifiez votre email et votre mot de passe.';
        } else if (err.status === 0 || err.status >= 500) {
          this.errorMessage = 'Serveur indisponible — utilisez un accès rapide ci-dessous.';
          this.showDemo = true;
        } else {
          this.errorMessage = 'Erreur de connexion. Veuillez réessayer ultérieurement.';
        }
      },
    });
  }

  fillAccount(email: string): void {
    this.signInForm.patchValue({ email, password: 'password123' });
  }

  connexionRapide(role: DemoRole): void {
    this.loading = true;
    this.errorMessage = null;
    this.authService.loginDemo(role).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Erreur de connexion rapide';
      },
    });
  }
}
