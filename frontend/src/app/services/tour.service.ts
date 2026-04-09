import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Tour, TourRequest } from '../models/tour.model';
import { AuthService } from './auth';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TourService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8080/api/tours';

  // State Signals
  private toursSignal = signal<Tour[]>([]);
  private selectedTourSignal = signal<Tour | null>(null);

  public isLoading = signal<boolean>(false);
  public errorMessage = signal<string>('');

  // Computed Values
  public tours = computed(() => this.toursSignal());
  public selectedTour = computed(() => this.selectedTourSignal());
  public hasTours = computed(() => this.toursSignal().length > 0);
  public selectedTourId = computed(() => this.selectedTourSignal()?.id ?? null);

  private handleError(err: HttpErrorResponse, defaultMsg: string): void {
    if (err.status === 403 || err.status === 401) {
      this.authService.logout();
      return;
    }
    this.errorMessage.set(err.error?.error ?? defaultMsg);
    this.isLoading.set(false);
  }

  public loadTours(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.get<Tour[]>(this.apiUrl).subscribe({
      next: (tours) => {
        this.toursSignal.set(tours);
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Laden der Touren.')
    });
  }

  public selectTour(tour: Tour | null): void {
    this.selectedTourSignal.set(tour);
  }

  public createTour(req: TourRequest, imageFile: File | null = null): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.post<Tour>(this.apiUrl, req).subscribe({
      next: (newTour) => {
        if (imageFile) {
          this.uploadImage(newTour.id, imageFile).then((tourWithImage) => {
            this.toursSignal.update(tours => [tourWithImage, ...tours]);
            this.selectTour(tourWithImage);
            this.isLoading.set(false);
          }).catch(err => {
            this.toursSignal.update(tours => [newTour, ...tours]);
            this.selectTour(newTour);
            this.handleError(err, 'Tour erstellt, aber Bild konnte nicht hochgeladen werden.');
          });
        } else {
          this.toursSignal.update(tours => [newTour, ...tours]);
          this.selectTour(newTour);
          this.isLoading.set(false);
        }
      },
      error: (err) => this.handleError(err, 'Fehler beim Erstellen der Tour.')
    });
  }

  public updateTour(id: number, req: TourRequest, imageFile: File | null = null): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.put<Tour>(`${this.apiUrl}/${id}`, req).subscribe({
      next: (baseUpdatedTour) => {
        if (imageFile) {
          this.uploadImage(id, imageFile).then((tourWithNewImage) => {
            const finalUpdatedTour = { ...tourWithNewImage, tourImagePath: tourWithNewImage.tourImagePath + '?t=' + new Date().getTime() };
            this.updateTourState(id, finalUpdatedTour);
          }).catch(err => {
            this.updateTourState(id, baseUpdatedTour);
            this.handleError(err, 'Tour aktualisiert, aber Bild konnte nicht hochgeladen werden.');
          });
        } else {
          this.updateTourState(id, baseUpdatedTour);
        }
      },
      error: (err) => this.handleError(err, 'Fehler beim Aktualisieren der Tour.')
    });
  }

  private updateTourState(id: number, updatedTour: Tour) {
    this.toursSignal.update(tours =>
      tours.map(t => t.id === id ? updatedTour : t)
    );

    if (this.selectedTourId() === id) {
      this.selectTour(null);
      setTimeout(() => this.selectTour(updatedTour), 1);
    }
    this.isLoading.set(false);
  }

  private uploadImage(tourId: number, file: File): Promise<Tour> {
    return new Promise((resolve, reject) => {
      const formData = new FormData();
      formData.append('file', file);
      this.http.post<Tour>(`${this.apiUrl}/${tourId}/image`, formData).subscribe({
        next: (tourWithNewImage) => resolve(tourWithNewImage),
        error: (err) => reject(err)
      });
    });
  }

  public getTourImage(tourId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${tourId}/image`, {
      responseType: 'blob'
    });
  }

  public deleteTour(id: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.delete(`${this.apiUrl}/${id}`).subscribe({
      next: () => {
        this.toursSignal.update(tours => tours.filter(t => t.id !== id));
        if (this.selectedTourId() === id) {
          this.selectTour(null);
        }
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Löschen der Tour.')
    });
  }

  public deleteTourImage(tourId: number): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.http.delete<Tour>(`${this.apiUrl}/${tourId}/image`).subscribe({
      next: (updatedTour) => {
        this.updateTourState(tourId, updatedTour);
        this.isLoading.set(false);
      },
      error: (err) => this.handleError(err, 'Fehler beim Löschen des Bildes.')
    });
  }
}
