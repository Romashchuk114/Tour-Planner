import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './login.scss',
  template: `
<div class="page">
  <h1 class="title">Anmelden</h1>

  <form class="form-card" [formGroup]="loginForm" (ngSubmit)="onSubmit()">
    @if (authService.errorMessage()) {
      <p class="error">{{ authService.errorMessage() }}</p>
    }

    <label class="field">
      <span class="label">Benutzername</span>
      <input type="text" placeholder="Eingabe" formControlName="usernameOrEmail" />
      @if (loginForm.get('usernameOrEmail')?.touched && loginForm.get('usernameOrEmail')?.hasError('required')) {
        <span class="field-error">Pflichtfeld</span>
      }
    </label>

    <label class="field">
      <span class="label">Passwort</span>
      <input type="password" placeholder="Eingabe" formControlName="password" />
      @if (loginForm.get('password')?.touched && loginForm.get('password')?.hasError('required')) {
        <span class="field-error">Pflichtfeld</span>
      }
    </label>

    <button type="submit" class="submit-btn">Anmelden</button>

    <p class="switch-link">
      Noch kein Konto? <a routerLink="/register">Registrieren</a>
    </p>
  </form>
</div>
  `
})
export class Login {
  protected authService = inject(AuthService);
  private fb = inject(FormBuilder);

  loginForm = this.fb.group({
    usernameOrEmail: ['', Validators.required],
    password: ['', Validators.required],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { usernameOrEmail, password } = this.loginForm.value;
    this.authService.login(usernameOrEmail!, password!);
  }
}
