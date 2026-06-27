import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TourService } from '../../../services/tour.service';
import {Tour} from '../../../models/tour.model';

@Component({
  selector: 'app-tour-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="tour-list-container">
      <div class="list-header">
        <h2>Touren</h2>
        <button class="add-btn" (click)="onNewTourClick()">+ Neue Tour</button>
      </div>

      <input
        type="search"
        class="search-input"
        placeholder="Touren durchsuchen…"
        [value]="searchQuery"
        (input)="onSearchInput($any($event.target).value)"
      >

      <hr>

      @if (tourService.isLoading()) {
        <div class="loading">Loading tours...</div>
      } @else if (tourService.errorMessage()) {
        <div class="error-msg">{{ tourService.errorMessage() }}</div>
      } @else if (!tourService.hasTours()) {
        <div class="empty-state">
          {{ searchQuery ? 'Keine Touren gefunden' : 'Keine Touren vorhanden' }}
        </div>
      } @else {
        <ul class="tour-list">
          @for (tour of tourService.tours(); track tour.id) {
            <li
              class="tour-item"
              [class.active]="tourService.selectedTourId() === tour.id"
              (click)="onSelectTour(tour)"
            >
              <span class="tour-name">{{ tour.name }}</span>
              <span class="tour-meta">{{ formatLocation(tour.fromName) }} &rarr; {{ formatLocation(destinationName(tour)) }}</span>
            </li>
          }
        </ul>
      }
    </div>
  `,
  styleUrls: ['./tours-list.scss']
})
export class ToursList {
  @Output() newTour = new EventEmitter<void>();

  public tourService = inject(TourService);

  searchQuery = '';
  private searchDebounce: ReturnType<typeof setTimeout> | null = null;

  onSearchInput(value: string): void {
    this.searchQuery = value;
    if (this.searchDebounce) {
      clearTimeout(this.searchDebounce);
    }
    this.searchDebounce = setTimeout(() => this.tourService.loadTours(this.searchQuery), 300);
  }

  onSelectTour(tour: Tour): void {
    this.tourService.selectTour(tour);
  }

  onNewTourClick(): void {
    this.newTour.emit();
  }

  destinationName(tour: Tour): string {
    return tour.stages[tour.stages.length - 1]?.endName ?? '';
  }

  formatLocation(location: string | undefined): string {
    if (!location) return '';
    return location.split(',')[0].trim();
  }
}
