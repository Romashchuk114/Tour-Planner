import { Component, inject, OnInit, effect, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToursList } from './tour-list/tours-list';
import { ToursDetail } from './tour-detail/tours-detail';
import { TourFormComponent } from './tour-form/tour-form';
import { TourLogFormComponent } from './tour-log-form/tour-log-form';
import { TourService } from '../../services/tour.service';
import { TourLogService } from '../../services/tour.log.service';
import { Tour, TourRequest } from '../../models/tour.model';
import { TourLog, TourLogRequest } from '../../models/tour-log.model';

@Component({
  selector: 'app-tours',
  standalone: true,
  imports: [CommonModule, ToursList, ToursDetail, TourFormComponent, TourLogFormComponent],
  template: `
    <div class="tours-layout">

      <!-- List View -->
      <div class="list-pane" [hidden]="isMobile() && showDetail()">
        <app-tour-list (newTour)="onNewTour()"></app-tour-list>
      </div>

      <!-- Detail View -->
      <div class="detail-pane" [hidden]="isMobile() && !showDetail()">

        <!-- Mobile Back Button -->
        @if (isMobile() && showDetail()) {
          <button
            class="back-btn mobile-only"
            (click)="onBackToList()"
          >
            &larr; Zurück zu den Touren
          </button>
        }

        <app-tour-detail
          (editTour)="onEditTour($event)"
          (createLog)="onCreateLog()"
          (editLog)="onEditLog($event)"
          (deleteLog)="onDeleteLog($event)"
        ></app-tour-detail>
      </div>

    </div>

    <!-- Tour Form Modal -->
    @if (isTourFormOpen) {
      <app-tour-form
        [tour]="editingTour"
        (saved)="onSaveTour($event)"
        (cancelled)="onCancelTourForm()"
      ></app-tour-form>
    }

    <!-- Tour Log Form Modal -->
    @if (isLogFormOpen) {
      <app-tour-log-form
        [log]="editingLog"
        (saved)="onSaveLog($event)"
        (cancelled)="onCancelLogForm()"
      ></app-tour-log-form>
    }
  `,
  styleUrls: ['./tours.scss']
})
export class Tours implements OnInit {
  public tourService = inject(TourService);
  public logService = inject(TourLogService);
  private ngZone = inject(NgZone);

  isTourFormOpen = false;
  editingTour: Tour | null = null;

  isLogFormOpen = false;
  editingLog: TourLog | null = null;

  private mobileBreakpoint = 767;
  public screenWidth = window.innerWidth;

  constructor() {
    // Listen to changes in the selected tour to fetch logs automatically
    effect(() => {
      const tourId = this.tourService.selectedTourId();
      if (tourId) {
        this.logService.loadLogs(tourId);
      } else {
        this.logService.clearLogs();
      }
    });

    // Resize listener with NgZone so Angular knows the variable changed
    window.addEventListener('resize', () => {
      this.ngZone.run(() => {
        this.screenWidth = window.innerWidth;
      });
    });
  }

  ngOnInit(): void {
    this.tourService.loadTours();
  }

  isMobile(): boolean {
    return this.screenWidth <= this.mobileBreakpoint;
  }

  showDetail(): boolean {
    return this.tourService.selectedTourId() !== null;
  }

  onBackToList(): void {
    this.tourService.selectTour(null);
  }

  // --- TOUR ACTIONS ---

  onNewTour(): void {
    this.editingTour = null;
    this.isTourFormOpen = true;
  }

  onEditTour(tour: Tour): void {
    this.editingTour = tour;
    this.isTourFormOpen = true;
  }

  onSaveTour(request: TourRequest): void {
    if (this.editingTour) {
      this.tourService.updateTour(this.editingTour.id, request);
    } else {
      this.tourService.createTour(request);
    }
    this.isTourFormOpen = false;
  }

  onCancelTourForm(): void {
    this.isTourFormOpen = false;
  }

  // --- LOG ACTIONS ---

  onCreateLog(): void {
    this.editingLog = null;
    this.isLogFormOpen = true;
  }

  onEditLog(log: TourLog): void {
    this.editingLog = log;
    this.isLogFormOpen = true;
  }

  onSaveLog(request: TourLogRequest): void {
    const tourId = this.tourService.selectedTourId();
    if (!tourId) return;

    if (this.editingLog) {
      this.logService.updateLog(tourId, this.editingLog.id, request);
    } else {
      this.logService.createLog(tourId, request);
    }
    this.isLogFormOpen = false;
  }

  onDeleteLog(logId: number): void {
    const tourId = this.tourService.selectedTourId();
    if (!tourId) return;
    this.logService.deleteLog(tourId, logId);
  }

  onCancelLogForm(): void {
    this.isLogFormOpen = false;
  }
}
