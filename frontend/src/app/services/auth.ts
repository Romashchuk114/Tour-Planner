import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface AuthResponse extends User {
  token: string;
}

@Injectable({
  providedIn: 'root',
})

export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'auth_token';

  private currentUser = signal<User | null>(this.loadUserFromToken());
  errorMessage = signal('');

  isLoggedIn = computed(() => this.currentUser() !== null);
  user = computed(() => this.currentUser());

  login(usernameOrEmail: string, password: string): void {
    this.errorMessage.set('');
    this.http.post<AuthResponse>(`${this.apiUrl}/login`, { usernameOrEmail, password }).subscribe({
      next: (response) => {
        this.handleAuthSuccess(response);
        this.router.navigate(['/tours']);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.error ?? 'Anmeldung fehlgeschlagen.');
      },
    });
  }

  register(username: string, email: string, password: string): void {
    this.errorMessage.set('');
    this.http.post<AuthResponse>(`${this.apiUrl}/register`, { username, email, password }).subscribe({
      next: (response) => {
        this.handleAuthSuccess(response);
        this.router.navigate(['/tours']);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.error ?? 'Registrierung fehlgeschlagen.');
      },
    });
  }

  logout(): void {
    this.currentUser.set(null);
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private handleAuthSuccess(response: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    const user: User = { id: response.id, username: response.username, email: response.email };
    this.currentUser.set(user);
  }

  private loadUserFromToken(): User | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (payload.exp * 1000 < Date.now()) {
        localStorage.removeItem(this.TOKEN_KEY);
        return null;
      }

      return { id: parseInt(payload.sub, 10), username: payload.username, email: '' };
    } catch (e) {
      localStorage.removeItem(this.TOKEN_KEY);
      return null;
    }
  }
}
