import { Component, EventEmitter, Output, inject, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import { Tour } from '../../../models/tour.model';
import { TRANSPORT_LABEL } from '../../../models/transport-types';
import { TourLogListComponent } from '../tour-log-list/tour-log-list';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { TourLog } from '../../../models/tour-log.model';
import { MapPoint, MapSegment, RouteMapComponent } from '../../../components/route-map/route-map.component';
import { segmentColor } from '../../../components/route-map/map-colors';

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
              {{ showFullAddress ? tour.fromName : formatLocation(tour.fromName) }}
              @for (stop of intermediateStops(tour); track $index) {
                &rarr; {{ showFullAddress ? stop : formatLocation(stop) }}
              }
              &rarr;
              {{ showFullAddress ? destinationName(tour) : formatLocation(destinationName(tour)) }}
            </span>
          </div>
          @if (tour.stages.length > 1) {
            <div class="info-item">
              <span class="label">Zwischenstopps:</span>
              <span class="value">{{ tour.stages.length - 1 }}</span>
            </div>
          }
          @if (tour.totalDistance) {
            <div class="info-item">
              <span class="label">Distanz gesamt:</span>
              <span class="value">{{ tour.totalDistance }} km</span>
            </div>
          }
          @if (tour.totalDuration) {
            <div class="info-item">
              <span class="label">Dauer gesamt:</span>
              <span class="value">{{ formatDuration(tour.totalDuration) }}</span>
            </div>
          }
        </div>

        @if (tour.description) {
          <div class="description">
            <h3>Beschreibung</h3>
            <p>{{ tour.description }}</p>
          </div>
        }

        <!-- Stages Table -->
        @if (tour.stages.length) {
          <div class="segments-section">
            <h3>Etappen</h3>
            <table class="segments-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Von</th>
                  <th>Nach</th>
                  <th>Transport</th>
                  <th>Distanz</th>
                  <th>Dauer</th>
                </tr>
              </thead>
              <tbody>
                @for (stage of tour.stages; track stage.orderIndex; let i = $index) {
                  <tr>
                    <td><span class="seg-badge" [style.background-color]="segmentColor(i)">{{ i + 1 }}</span></td>
                    <td>{{ stageFromLabel(tour, i) }}</td>
                    <td>{{ formatLocation(stage.endName) }}</td>
                    <td>{{ transportLabel[stage.transportType] }}</td>
                    <td>{{ stage.distance ? stage.distance + ' km' : '—' }}</td>
                    <td>{{ formatDuration(stage.duration) || '—' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }

        <!-- Route Map -->
        <div class="map-section">
          <h3>Route</h3>
          <app-route-map
            [points]="mapPoints()"
            [segments]="mapSegments()">
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

  protected readonly transportLabel = TRANSPORT_LABEL;
  protected readonly segmentColor = segmentColor;

  mapPoints = computed<MapPoint[]>(() => {
    const tour = this.tourService.selectedTour();
    if (!tour) return [];
    const points: MapPoint[] = [
      { lat: tour.fromLat, lng: tour.fromLng, label: tour.fromName, kind: 'start' }
    ];
    tour.stages.forEach((s, i) => {
      const isLast = i === tour.stages.length - 1;
      points.push({
        lat: s.endLat,
        lng: s.endLng,
        label: s.endName,
        kind: isLast ? 'end' : 'waypoint',
        index: isLast ? undefined : i + 1
      });
    });
    return points;
  });

  mapSegments = computed<MapSegment[]>(() => {
    const tour = this.tourService.selectedTour();
    return tour?.stages.map(s => ({
      geometryGeoJson: s.geometryGeoJson,
      transportType: TRANSPORT_LABEL[s.transportType]
    })) ?? [];
  });

  intermediateStops(tour: Tour): string[] {
    return tour.stages.slice(0, -1).map(s => s.endName);
  }

  destinationName(tour: Tour): string {
    return tour.stages[tour.stages.length - 1]?.endName ?? '';
  }

  stageFromLabel(tour: Tour, i: number): string {
    return i === 0
      ? this.formatLocation(tour.fromName)
      : this.formatLocation(tour.stages[i - 1].endName);
  }

  onEditClick(tour: Tour): void {
    this.editTour.emit(tour);
  }

  onDelete(): void {
    const tourId = this.tourService.selectedTourId();
    if (tourId && confirm('Sind Sie sicher, dass Sie diese Tour löschen möchten?')) {
      this.tourService.deleteTour(tourId);
    }
  }

  toggleAddressDisplay(): void {
    this.showFullAddress = !this.showFullAddress;
  }

  formatLocation(location: string | undefined): string {
    if (!location) return '';
    return location.split(',')[0].trim();
  }

  formatDuration(minutes: number | null | undefined): string {
    if (minutes === null || minutes === undefined) return '';
    if (minutes < 60) return `${minutes} min`;

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (hours < 24) {
      return remainingMinutes === 0 ? `${hours} h` : `${hours} h ${remainingMinutes} min`;
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
