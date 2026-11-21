import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const p = group.get('password')?.value;
  const c = group.get('confirmPassword')?.value;
  if (!p || !c) return null;
  return p === c ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './inscription.component.html',
})
export class InscriptionComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  categories = ['B', 'A', 'A1', 'A2', 'C', 'D', 'EB', 'EC', 'ED'];

  form: FormGroup = this.fb.group(
    {
      nom: ['', [Validators.required, Validators.minLength(2)]],
      prenom: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: [''],
      cin: ['', [Validators.required, Validators.minLength(4)]],
      categoriePermis: ['B', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch }
  );

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = null;
    this.successMessage = null;

    const v = this.form.value;
    this.auth
      .inscription({
        nom: v.nom,
        prenom: v.prenom,
        email: v.email,
        telephone: v.telephone || null,
        password: v.password,
        cin: v.cin,
        categoriePermis: v.categoriePermis,
        forfaitId: null,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.successMessage = 'Compte créé — redirection…';
          setTimeout(() => this.router.navigate(['/dashboard']), 600);
        },
        error: (err) => {
          this.loading = false;
          const msg = err?.error?.message || err?.error?.error;
          if (err.status === 0 || err.status >= 500) {
            this.errorMessage =
              'Serveur indisponible. Vérifiez que le backend tourne, ou connectez-vous en démo.';
          } else if (typeof msg === 'string' && msg.trim()) {
            this.errorMessage = msg;
          } else if (err.status === 400) {
            this.errorMessage = 'Données invalides — vérifiez le CIN, l’email et le formulaire.';
          } else {
            this.errorMessage = 'Inscription impossible. Réessayez ou connectez-vous.';
          }
        },
      });
  }
}
