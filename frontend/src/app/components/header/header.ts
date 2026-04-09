import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  template: `
    <header class="header">
      <a routerLink="/" class="logo">
        <img src="Logo.png" alt="Tour Planner Logo" class="logo-img" />
      </a>

      <nav class="nav">
        @if (authService.isLoggedIn()) {
          <a routerLink="/tours" class="nav-link">Meine Touren</a>
          <span class="username">{{ authService.user()?.username }}</span>
          <button class="btn btn-outline" (click)="onSignOut()">Abmelden</button>
        } @else {
          <a routerLink="/login" class="btn btn-outline">Anmelden</a>
          <a routerLink="/register" class="btn btn-filled">Registrieren</a>
        }
      </nav>
    </header>
  `,
  styleUrl: './header.scss',
})
export class Header {
  protected authService = inject(AuthService);

  onSignOut(): void {
    this.authService.logout();
  }
}
