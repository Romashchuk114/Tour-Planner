import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToursList } from './tour-list/tours-list';
import { ToursDetail } from './tour-detail/tours-detail';
import { TourFormComponent } from './tour-form/tour-form';
import { TourService } from '../../services/tour.service';
import { Tour, TourRequest } from '../../models/tour.model';

@Component({
  selector: 'app-tours',
  standalone: true,
  imports: [CommonModule, ToursList, ToursDetail, TourFormComponent],
  template: `
    <div class="tours-layout" [class.show-detail]="showDetail()">

      <!-- List View -->
      <div class="list-pane">
        <app-tour-list (newTour)="onNewTour()"></app-tour-list>
      </div>

      <!-- Detail View -->
      <div class="detail-pane">

        <!-- Mobile Back Button -->
        @if (showDetail()) {
          <button
            class="back-btn mobile-only"
            (click)="onBackToList()"
          >
            &larr; Back to Tours
          </button>
        }

        <app-tour-detail (editTour)="onEditTour($event)"></app-tour-detail>
      </div>

    </div>

    <!-- Tour Form Modal -->
    @if (isFormOpen) {
      <app-tour-form
        [tour]="editingTour"
        (saved)="onSaveTour($event)"
        (cancelled)="onCancelForm()"
      ></app-tour-form>
    }
  `,
  styleUrls: ['./tours.scss']
})
export class Tours implements OnInit {
  public tourService = inject(TourService);

  isFormOpen = false;
  editingTour: Tour | null = null;

  ngOnInit(): void {
    this.tourService.loadTours();
  }

  showDetail(): boolean {
    return this.tourService.selectedTourId() !== null;
  }

  onBackToList(): void {
    this.tourService.selectTour(null);
  }

  onNewTour(): void {
    this.editingTour = null;
    this.isFormOpen = true;
  }

  onEditTour(tour: Tour): void {
    this.editingTour = tour;
    this.isFormOpen = true;
  }

  onSaveTour(request: TourRequest): void {
    if (this.editingTour) {
      this.tourService.updateTour(this.editingTour.id, request);
    } else {
      this.tourService.createTour(request);
    }
    this.isFormOpen = false;
  }

  onCancelForm(): void {
    this.isFormOpen = false;
  }
}
