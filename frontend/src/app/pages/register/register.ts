import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './register.scss',
  template: `
<div class="page">
  <h1 class="title">Registrieren</h1>

  <form class="form-card" [formGroup]="registerForm" (ngSubmit)="onSubmit()">
    @if (authService.errorMessage()) {
      <p class="error">{{ authService.errorMessage() }}</p>
    }

    <label class="field">
      <span class="label">Benutzername</span>
      <input type="text" placeholder="Eingabe" formControlName="username" />
      @if (registerForm.get('username')?.touched && registerForm.get('username')?.hasError('required')) {
        <span class="field-error">Pflichtfeld</span>
      }
    </label>

    <label class="field">
      <span class="label">E-Mail</span>
      <input type="email" placeholder="Eingabe" formControlName="email" />
      @if (registerForm.get('email')?.touched && registerForm.get('email')?.hasError('required')) {
        <span class="field-error">Pflichtfeld</span>
      } @else if (registerForm.get('email')?.touched && registerForm.get('email')?.hasError('email')) {
        <span class="field-error">Ungültige E-Mail-Adresse</span>
      }
    </label>

    <label class="field">
      <span class="label">Passwort</span>
      <input type="password" placeholder="Eingabe" formControlName="password" />
      @if (registerForm.get('password')?.touched && registerForm.get('password')?.hasError('required')) {
        <span class="field-error">Pflichtfeld</span>
      } @else if (registerForm.get('password')?.touched && registerForm.get('password')?.hasError('minlength')) {
        <span class="field-error">Mindestens 6 Zeichen</span>
      }
    </label>

    <button type="submit" class="submit-btn">Registrieren</button>

    <p class="switch-link">
      Bereits ein Konto? <a routerLink="/login">Anmelden</a>
    </p>
  </form>
</div>
  `
})
export class Register {
  protected authService = inject(AuthService);
  private fb = inject(FormBuilder);

  registerForm = this.fb.group({
    username: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const { username, email, password } = this.registerForm.value;
    this.authService.register(username!, email!, password!);
  }
}
