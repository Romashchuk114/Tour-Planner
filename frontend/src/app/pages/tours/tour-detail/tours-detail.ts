import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import { Tour } from '../../../models/tour.model';
import { TourLogListComponent } from '../tour-log-list/tour-log-list';

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
            (editLog)="onEditLog.emit($event)"
            (createLog)="onCreateLog.emit()"
            (deleteLogEmit)="onDeleteLog.emit($event)"
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
  @Output() editLog = new EventEmitter<any>();
  @Output() createLog = new EventEmitter<void>();
  @Output() deleteLog = new EventEmitter<number>();

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
    }
  }

  onEditLog = new EventEmitter<any>();
  onCreateLog = new EventEmitter<void>();
  onDeleteLog = new EventEmitter<number>();

  constructor() {
    this.onEditLog.subscribe((log) => this.editLog.emit(log));
    this.onCreateLog.subscribe(() => this.createLog.emit());
    this.onDeleteLog.subscribe((id) => this.deleteLog.emit(id));
  }
}
