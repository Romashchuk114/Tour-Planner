import { Injectable, signal, computed } from '@angular/core';

export interface AppNotification {
  type: 'success' | 'error';
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSignal = signal<AppNotification | null>(null);

  public notification = computed(() => this.notificationSignal());

  success(text: string): void {
    this.notificationSignal.set({ type: 'success', text });
  }

  error(text: string): void {
    this.notificationSignal.set({ type: 'error', text });
  }

  dismiss(): void {
    this.notificationSignal.set(null);
  }
}
