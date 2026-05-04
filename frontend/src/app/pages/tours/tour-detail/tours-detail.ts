import { Component, EventEmitter, Output, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import { Tour } from '../../../models/tour.model';
import { TourLogListComponent } from '../tour-log-list/tour-log-list';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {TourLog} from '../../../models/tour-log.model';
import { RouteMapComponent } from '../../../components/route-map/route-map.component';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule, TourLogListComponent, RouteMapComponent],
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
          <div class="info-item clickable-route" (click)="toggleAddressDisplay()" title="Klicken um vollständige/abgekürzte Adresse anzuzeigen">
            <span class="label">Route:</span>
            <span class="value">
              {{ showFullAddress ? tour.fromLocation : formatLocation(tour.fromLocation) }}
              &rarr;
              {{ showFullAddress ? tour.toLocation : formatLocation(tour.toLocation) }}
            </span>
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
              <span class="value">{{ formatDuration(tour.estimatedTime) }}</span>
            </div>
          }
        </div>

        @if (tour.description) {
          <div class="description">
            <h3>Beschreibung</h3>
            <p>{{ tour.description }}</p>
          </div>
        }

        <!-- Route Map -->
        <div class="map-section">
          <h3>Route</h3>
          <app-route-map
            [routeGeometry]="tour.routeGeometry"
            [fromLat]="tour.fromLat"
            [fromLng]="tour.fromLng"
            [toLat]="tour.toLat"
            [toLng]="tour.toLng"
            [fromLabel]="tour.fromLocation"
            [toLabel]="tour.toLocation">
          </app-route-map>
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

  public showFullAddress = false;

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

  toggleAddressDisplay(): void {
    this.showFullAddress = !this.showFullAddress;
  }

  getTransportName(type: string): string {
    switch (type) {
      case 'WALK': return 'Zu Fuß';
      case 'HIKING': return 'Wandern';
      case 'BIKE': return 'Fahrrad';
      case 'MOUNTAIN_BIKE': return 'Mountainbike';
      case 'ROAD_BIKE': return 'Rennrad';
      case 'CAR': return 'Auto';
      case 'MOTORHOME': return 'Wohnmobil / LKW';
      default: return type;
    }
  }

  formatLocation(location: string | undefined): string {
    if (!location) return '';
    return location.split(',')[0].trim();
  }

  formatDuration(minutes: number | null | undefined): string {
    if (minutes === null || minutes === undefined) return '';

    if (minutes < 60) {
      return `${minutes} min`;
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (hours < 24) {
      if (remainingMinutes === 0) return `${hours} h`;
      return `${hours} h ${remainingMinutes} min`;
    }

    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;

    let result = `${days} d`;
    if (remainingHours > 0) result += ` ${remainingHours} h`;
    if (remainingMinutes > 0) result += ` ${remainingMinutes} min`;

    return result;
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

      // Reset address display when changing tours
      if (tourId) {
        this.showFullAddress = false;
      }

      if (tourId && imagePath) {
        this.loadImage(tourId);
      } else {
        this.imageUrl = null;
      }
    });
  }
}
