import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import { Tour } from '../../../models/tour.model';

@Component({
  selector: 'app-tour-detail',
  standalone: true,
  imports: [CommonModule],
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

        <div class="info-grid">
          <div class="info-item">
            <span class="label">Route:</span>
            <span class="value">{{ tour.fromLocation }} &rarr; {{ tour.toLocation }}</span>
          </div>
          <div class="info-item">
            <span class="label">Transportart:</span>
            <span class="value">{{ tour.transportType }}</span>
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
            <h3>Bearbeiten</h3>
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

        <!-- Tour Logs Placeholder -->
        <div class="logs-section">
          <h3>Tour Logs ({{ tour.logCount || 0 }})</h3>
          <div class="logs-placeholder">
            Logs component will go here
          </div>
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

  public tourService = inject(TourService);

  onEditClick(tour: Tour): void {
    this.editTour.emit(tour);
  }

  onDelete(): void {
    const tourId = this.tourService.selectedTourId();
    if (tourId) {
      if (confirm('Are you sure you want to delete this tour?')) {
        this.tourService.deleteTour(tourId);
      }
      // TODO: Replace with ConfirmDialogComponent
    }
  }
}
