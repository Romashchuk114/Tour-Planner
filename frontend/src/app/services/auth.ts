import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

export interface User {
  id: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = 'http://localhost:8080/api/auth';

  // TODO: Testcode — sessionStorage durch JWT-basierte Auth ersetzen
  private currentUser = signal<User | null>(this.loadUser());
  errorMessage = signal('');

  isLoggedIn = computed(() => this.currentUser() !== null);
  user = computed(() => this.currentUser());

  login(usernameOrEmail: string, password: string): void {
    this.errorMessage.set('');
    this.http.post<User>(`${this.apiUrl}/login`, { usernameOrEmail, password }).subscribe({
      next: (user) => {
        this.setUser(user);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.error ?? 'Anmeldung fehlgeschlagen.');
      },
    });
  }

  register(username: string, email: string, password: string): void {
    this.errorMessage.set('');
    this.http.post<User>(`${this.apiUrl}/register`, { username, email, password }).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.error ?? 'Registrierung fehlgeschlagen.');
      },
    });
  }

  logout(): void {
    this.currentUser.set(null);
    sessionStorage.removeItem('user'); // TODO: Testcode — durch JWT-Logout ersetzen
    this.router.navigate(['/login']);
  }

  // TODO: Testcode — durch JWT-Token-Management ersetzen
  private setUser(user: User): void {
    this.currentUser.set(user);
    sessionStorage.setItem('user', JSON.stringify(user));
  }

  // TODO: Testcode — durch JWT-Token-Validierung ersetzen
  private loadUser(): User | null {
    const data = sessionStorage.getItem('user');
    return data ? JSON.parse(data) : null;
  }
}