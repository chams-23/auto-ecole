import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { CandidatService } from '../../core/services/candidat.service';

@Component({
  selector: 'app-candidat-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './candidat-form.component.html',
})
export class CandidatFormComponent implements OnInit {
  candidatForm: FormGroup;
  mode: 'nouveau' | 'modifier' = 'nouveau';
  candidatId?: number;
  loading = false;
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  categories = [
    { value: 'B', label: 'B - Voiture légère' },
    { value: 'C', label: 'C - Poids lourd' },
    { value: 'D', label: 'D - Transport en commun' },
    { value: 'EB', label: 'EB - B + Remorque' },
    { value: 'EC', label: 'EC - C + Remorque' },
    { value: 'ED', label: 'ED - D + Remorque' },
    { value: 'A', label: 'A - Moto' },
    { value: 'A1', label: 'A1 - Moto légère' },
    { value: 'A2', label: 'A2 - Moto moyenne' },
  ];

  forfaits = [
    { value: 1, label: 'B Standard - 3 500,00 DH (25h)' },
    { value: 2, label: 'C Premium - 5 500,00 DH (30h)' },
  ];

  constructor(
    private fb: FormBuilder,
    private candidatService: CandidatService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.candidatForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      cin: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9]{3,15}$/)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^(\+212|00212|0)\d{9,10}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      categoriePermis: ['B', Validators.required],
      forfaitId: [1, Validators.required],
    }, { validators: this.passwordsMatchValidator });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.mode = 'modifier';
      this.candidatId = +id;
      this.candidatForm.get('password')?.clearValidators();
      this.candidatForm.get('confirmPassword')?.clearValidators();
      this.candidatForm.get('password')?.updateValueAndValidity();
      this.candidatForm.get('confirmPassword')?.updateValueAndValidity();
      this.loadCandidat();
    }
  }

  loadCandidat(): void {
    if (!this.candidatId) return;
    this.candidatService.getById(this.candidatId).subscribe({
      next: (c) => {
        this.candidatForm.patchValue({
          nom: c.nom,
          prenom: c.prenom,
          cin: c.cin,
          email: c.email,
          telephone: c.telephone,
          categoriePermis: c.categoriePermis,
          forfaitId: c.forfaitId,
        });
      },
      error: () => {}
    });
  }

  passwordsMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const pwd = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    if (this.mode === 'modifier' && !pwd && !confirm) return null;
    return pwd && confirm && pwd === confirm ? null : { passwordsMismatch: true };
  };

  onSubmit() {
    if (this.candidatForm.invalid) {
      Object.values(this.candidatForm.controls).forEach(c => c.markAsTouched());
      this.showErrorToast('Veuillez corriger les erreurs dans le formulaire');
      return;
    }
    this.loading = true;

    const raw = this.candidatForm.value;

    if (this.mode === 'nouveau') {
      const formData = {
        nom: raw.nom?.trim(),
        prenom: raw.prenom?.trim(),
        cin: raw.cin?.trim(),
        email: raw.email?.trim().toLowerCase(),
        telephone: raw.telephone?.trim(),
        password: raw.password,
        categoriePermis: raw.categoriePermis,
        forfaitId: raw.forfaitId != null ? Number(raw.forfaitId) : null,
      };

      this.candidatService.creer(formData).subscribe({
        next: () => {
          this.loading = false;
          this.toastType = 'success';
          this.toastMessage = 'Candidat créé avec succès';
          this.showToast = true;
          setTimeout(() => {
            this.showToast = false;
            this.router.navigate(['/candidats']);
          }, 1500);
        },
        error: (err) => {
          this.loading = false;
          const rawMsg = err?.error?.message
            || (err?.error?.errors && Object.values(err.error.errors).join(' — '))
            || (typeof err?.message === 'string' ? err.message : null)
            || null;
          this.showErrorToast(rawMsg || 'Erreur lors de l\'enregistrement');
        }
      });
    } else {
      const formData: any = {};
      if (raw.nom != null) formData.nom = raw.nom.trim();
      if (raw.prenom != null) formData.prenom = raw.prenom.trim();
      if (raw.cin != null) formData.cin = raw.cin.trim();
      if (raw.email != null) formData.email = raw.email.trim().toLowerCase();
      if (raw.telephone != null) formData.telephone = raw.telephone.trim();
      if (raw.password && raw.password.length > 0) formData.password = raw.password;
      if (raw.categoriePermis != null) formData.categoriePermis = raw.categoriePermis;
      if (raw.forfaitId != null) formData.forfaitId = Number(raw.forfaitId);

      this.candidatService.modifier(this.candidatId!, formData).subscribe({
        next: () => {
          this.loading = false;
          this.toastType = 'success';
          this.toastMessage = 'Candidat modifié avec succès';
          this.showToast = true;
          setTimeout(() => {
            this.showToast = false;
            this.router.navigate(['/candidats']);
          }, 1500);
        },
        error: (err) => {
          this.loading = false;
          const rawMsg = err?.error?.message
            || (err?.error?.errors && Object.values(err.error.errors).join(' — '))
            || (typeof err?.message === 'string' ? err.message : null)
            || null;
          this.showErrorToast(rawMsg || 'Erreur lors de l\'enregistrement');
        }
      });
    }
  }

  private showErrorToast(msg: string): void {
    this.toastType = 'error';
    this.toastMessage = msg;
    this.showToast = true;
    setTimeout(() => { this.showToast = false; }, 6000);
  }
}
