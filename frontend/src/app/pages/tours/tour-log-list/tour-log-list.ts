import { Component, EventEmitter, Output, inject, LOCALE_ID } from '@angular/core';
import { CommonModule, DatePipe, registerLocaleData } from '@angular/common';
import { TourLogService } from '../../../services/tour.log.service';
import { TourLog } from '../../../models/tour-log.model';
import localeDe from '@angular/common/locales/de';

registerLocaleData(localeDe);

@Component({
  selector: 'app-tour-log-list',
  standalone: true,
  imports: [CommonModule],
  providers: [
    DatePipe,
    { provide: LOCALE_ID, useValue: 'de-DE' }
  ],
  template: `
    <div class="log-list-container">
      <div class="list-header">
        <button class="add-btn" (click)="onNewLog()">+ Neues Log</button>
      </div>

      @if (logService.isLoading()) {
        <div class="loading">Logs laden...</div>
      } @else if (logService.errorMessage()) {
        <div class="error-msg">{{ logService.errorMessage() }}</div>
      } @else if (!logService.hasLogs()) {
        <div class="empty-state">Keine Logs für diese Tour</div>
      } @else {
        <div class="table-responsive">
          <table class="log-table">
            <thead>
              <tr>
                <th>Datum</th>
                <th>Distanz (km)</th>
                <th>Dauer (min)</th>
                <th>Schwierigkeit</th>
                <th>Bewertung</th>
                <th class="hide-mobile">Kommentar</th>
                <th>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              @for (log of logService.logs(); track log.id) {
                <tr>
                  <td>{{ log.dateTime | date:'dd.MM.yyyy, HH:mm' }}</td>
                  <td>{{ log.totalDistance }}</td>
                  <td>{{ log.totalTime }}</td>
                  <td>{{ log.difficulty }}/10</td>
                  <td>
                    <span class="rating">
                      @for (star of [1,2,3,4,5]; track star) {
                        <span [class.filled]="star <= log.rating">★</span>
                      }
                    </span>
                  </td>
                  <td
                    class="hide-mobile comment-cell clickable"
                    [title]="log.comment ? 'Klicken zum Lesen' : ''"
                    (click)="onShowComment(log.comment)"
                  >
                    {{ log.comment || '-' }}
                  </td>
                  <td class="actions">
                    <button class="edit-btn" (click)="onEditLog(log)">Bearbeiten</button>
                    <button class="delete-btn" (click)="onDeleteLog(log.id)">Löschen</button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <!-- Comment Modal -->
    @if (selectedComment !== null) {
      <div class="modal-overlay" (click)="closeComment()">
        <div class="modal-content comment-modal" (click)="$event.stopPropagation()">
          <h3>Kommentar lesen</h3>
          <div class="comment-text">
            {{ selectedComment || 'Kein Kommentar vorhanden.' }}
          </div>
          <div class="modal-actions">
            <button class="close-btn" (click)="closeComment()">Schließen</button>
          </div>
        </div>
      </div>
    }
  `,
  styleUrls: ['./tour-log-list.scss']
})
export class TourLogListComponent {
  public logService = inject(TourLogService);

  @Output() editLog = new EventEmitter<TourLog>();
  @Output() createLog = new EventEmitter<void>();
  @Output() deleteLogEmit = new EventEmitter<number>();

  selectedComment: string | null = null;

  onNewLog(): void {
    this.createLog.emit();
  }

  onEditLog(log: TourLog): void {
    this.editLog.emit(log);
  }

  onDeleteLog(logId: number): void {
    if (confirm('Sind Sie sicher, dass Sie diesen Log löschen möchten?')) {
      this.deleteLogEmit.emit(logId);
    }
  }

  onShowComment(comment: string | undefined): void {
    if (comment) {
      this.selectedComment = comment;
    }
  }

  closeComment(): void {
    this.selectedComment = null;
  }
}
