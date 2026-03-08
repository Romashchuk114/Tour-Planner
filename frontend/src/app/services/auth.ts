import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';

export interface User {
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private router = inject(Router);
  private currentUser = signal<User | null>(null);

  isLoggedIn = computed(() => this.currentUser() !== null);
  user = computed(() => this.currentUser());

  login(usernameOrEmail: string, password: string): boolean {
    // Mock: accept any non-empty credentials
    if (usernameOrEmail && password) {
      this.currentUser.set({ username: usernameOrEmail, email: '' });
      this.router.navigate(['/tours']);
      return true;
    }
    return false;
  }

  register(username: string, email: string, password: string): boolean {
    // Mock: accept any non-empty values
    if (username && email && password) {
      this.currentUser.set({ username, email });
      this.router.navigate(['/tours']);
      return true;
    }
    return false;
  }

  logout(): void {
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}