import { Component, EventEmitter, Output, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import { Tour } from '../../../models/tour.model';
import { TourLogListComponent } from '../tour-log-list/tour-log-list';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {TourLog} from '../../../models/tour-log.model';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, TourLogListComponent],
  template: `
    @if (tourService.selectedTour(); as tour) {
      <div class="tour-detail-container">
        <div class="detail-header">
          <h2>{{ tour.name }}</h2>
          <div class="actions">
            <button class="edit-btn" (click)="onEditClick(tour)">Bearbeiten</button>
            <button class="delete-btn" (click)="onDelete()">Löschen</button>
          </div>
        </div>

        <!-- Tour Image -->
        @if (tour.tourImagePath && imageUrl) {
          <div class="tour-image-container">
            <img [src]="imageUrl" alt="Tour Bild" class="tour-image">
          </div>
        }

        <div class="info-grid">
          <div class="info-item">
            <span class="label">Route:</span>
            <span class="value">{{ tour.fromLocation }} &rarr; {{ tour.toLocation }}</span>
          </div>
          <div class="info-item">
            <span class="label">Transportart:</span>
            <span class="value">{{ getTransportName(tour.transportType) }}</span>
          </div>
          @if (tour.tourDistance) {
            <div class="info-item">
              <span class="label">Distanz:</span>
              <span class="value">{{ tour.tourDistance }} km</span>
            </div>
          }
          @if (tour.estimatedTime) {
            <div class="info-item">
              <span class="label">Dauer:</span>
              <span class="value">{{ tour.estimatedTime }} min</span>
            </div>
          }
        </div>

        @if (tour.description) {
          <div class="description">
            <h3>Beschreibung</h3>
            <p>{{ tour.description }}</p>
          </div>
        }

        <!-- Map Placeholder -->
        <div class="map-section">
          <div class="map-placeholder">
            Map integration coming in final submission
            <br>
            <small>Route: {{ tour.fromLocation }} to {{ tour.toLocation }}</small>
          </div>
        </div>

        <!-- Tour Logs Section -->
        <div class="logs-section">
          <h3>Tour Logs</h3>
          <app-tour-log-list
            (editLog)="editLog.emit($event)"
            (createLog)="createLog.emit()"
            (deleteLogEmit)="deleteLog.emit($event)"
          ></app-tour-log-list>
        </div>
      </div>
    } @else {
      <div class="empty-state">
        Wähle eine Tour aus der Liste
      </div>
    }
  `,
  styleUrls: ['./tours-detail.scss']
})
export class ToursDetail {
  @Output() editTour = new EventEmitter<Tour>();
  @Output() editLog = new EventEmitter<TourLog>();
  @Output() createLog = new EventEmitter<void>();
  @Output() deleteLog = new EventEmitter<number>();

  public tourService = inject(TourService);
  private sanitizer = inject(DomSanitizer);

  public imageUrl: SafeUrl | null = null;
  private currentImageObjectUrl: string | null = null;

  onEditClick(tour: Tour): void {
    this.editTour.emit(tour);
  }

  onDelete(): void {
    const tourId = this.tourService.selectedTourId();
    if (tourId) {
      if (confirm('Sind Sie sicher, dass Sie diese Tour löschen möchten?')) {
        this.tourService.deleteTour(tourId);
      }
    }
  }

  getTransportName(type: string): string {
    switch (type) {
      case 'WALK': return 'Zu fuß';
      case 'CAR': return 'Auto';
      case 'PUBLIC_TRANSPORT': return 'Öffentlicher Verkehr';
      case 'BIKE': return 'Fahrrad';
      case 'RUNNING': return 'Laufen';
      default: return type;
    }
  }

  loadImage(tourId: number): void {
    if (this.currentImageObjectUrl) {
      URL.revokeObjectURL(this.currentImageObjectUrl);
      this.currentImageObjectUrl = null;
    }
    this.imageUrl = null;

    this.tourService.getTourImage(tourId).subscribe({
      next: (blob: Blob) => {
        if (blob && blob.size > 0) {
          this.currentImageObjectUrl = URL.createObjectURL(blob);
          this.imageUrl = this.sanitizer.bypassSecurityTrustUrl(this.currentImageObjectUrl);
        } else {
          this.imageUrl = null;
        }
      },
      error: () => {
         this.imageUrl = null;
      }
    });
  }

  constructor() {
    effect(() => {
      const tourId = this.tourService.selectedTourId();
      const tour = this.tourService.selectedTour();
      const imagePath = tour?.tourImagePath;

      if (tourId && imagePath) {
        this.loadImage(tourId);
      } else {
        this.imageUrl = null;
      }
    });
  }
}
