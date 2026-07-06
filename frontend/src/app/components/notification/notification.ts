import { Component, inject } from '@angular/core';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  template: `
    @if (notificationService.notification(); as notification) {
      <div class="notification-overlay" (click)="notificationService.dismiss()">
        <div
          class="notification"
          [class.success]="notification.type === 'success'"
          [class.error]="notification.type === 'error'"
          (click)="$event.stopPropagation()"
        >
          <div class="content">
            <h3 class="title">{{ notification.type === 'error' ? 'Fehler' : 'Erfolg' }}</h3>
            <span class="text">{{ notification.text }}</span>
          </div>
          <button class="close-btn" (click)="notificationService.dismiss()">&times;</button>
        </div>
      </div>
    }
  `,
  styleUrl: './notification.scss',
})
export class Notification {
  protected notificationService = inject(NotificationService);
}
