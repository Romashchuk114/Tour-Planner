import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { TourLog, TourLogRequest } from '../models/tour-log.model';
import { AuthService } from './auth';

@Injectable({
  providedIn: 'root'
})

export class TourLogService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8080/api/tours';

  // State Signals
  private logsSignal = signal<TourLog[]>([]);
  public isLoading = signal<boolean>(false);
  public errorMessage = signal<string>('');

  // Computed Values
  public logs = computed(() => this.logsSignal());
  public hasLogs = computed(() => this.logsSignal().length > 0);
  public logCount = computed(() => this.logsSignal().length);

  private handleError(err: HttpErrorResponse, defaultMsg: string): void {
    if (err.status === 403 || err.status === 401) {
      this.authService.logout();
      return;
    }
    this.errorMessage.set(err.error?.error ?? defaultMsg);
    this.isLoading.set(false);
  }

  public loadLogs(tourId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.get<TourLog[]>(`${this.apiUrl}/${tourId}/logs`).subscribe({
      next: (logs) => {
        this.logsSignal.set(logs);
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Laden der Logs.')
    });
  }

  public clearLogs(): void {
    this.logsSignal.set([]);
  }

  public createLog(tourId: number, req: TourLogRequest): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.post<TourLog>(`${this.apiUrl}/${tourId}/logs`, req).subscribe({
      next: (newLog) => {
        this.logsSignal.update(logs => [newLog, ...logs]);
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Erstellen des Logs.')
    });
  }

  public updateLog(tourId: number, logId: number, req: TourLogRequest): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.put<TourLog>(`${this.apiUrl}/${tourId}/logs/${logId}`, req).subscribe({
      next: (updatedLog) => {
        this.logsSignal.update(logs =>
          logs.map(l => l.id === logId ? updatedLog : l)
        );
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Aktualisieren des Logs.')
    });
  }

  public deleteLog(tourId: number, logId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.delete(`${this.apiUrl}/${tourId}/logs/${logId}`).subscribe({
      next: () => {
        this.logsSignal.update(logs => logs.filter(l => l.id !== logId));
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Löschen des Logs.')
    });
  }
}
