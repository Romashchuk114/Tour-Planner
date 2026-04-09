import { Component, EventEmitter, Input, Output, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Tour, TourRequest } from '../../../models/tour.model';
import { FormFieldComponent } from '../../../components/form-field/form-field.component';
import { TourService } from '../../../services/tour.service';

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormFieldComponent],
  template: `
    <div class="modal-overlay" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>{{ isEditMode ? 'Tour Bearbeiten' : 'Neue Tour' }}</h2>

        <form [formGroup]="tourForm" (ngSubmit)="onSubmit()">

          <app-form-field label="Name" [errorMessage]="getErrorMessage('name')">
            <input type="text" formControlName="name" placeholder="Tour name">
          </app-form-field>

          <app-form-field label="Von" [errorMessage]="getErrorMessage('fromLocation')">
            <input type="text" formControlName="fromLocation" placeholder="Start">
          </app-form-field>

          <app-form-field label="Nach" [errorMessage]="getErrorMessage('toLocation')">
            <input type="text" formControlName="toLocation" placeholder="Ziel">
          </app-form-field>

          <app-form-field label="Transport Typ" [errorMessage]="getErrorMessage('transportType')">
            <select formControlName="transportType">
              <option value="" disabled>Wähle Transport Typ</option>
              <option value="WALK">Zu Fuß</option>
              <option value="BIKE">Fahrrad</option>
              <option value="CAR">Auto</option>
              <option value="PUBLIC_TRANSPORT">Öffentliche Verkehrsmittel</option>
            </select>
          </app-form-field>

          <app-form-field label="Beschreibung" [errorMessage]="getErrorMessage('description')">
            <textarea formControlName="description" rows="3" placeholder="Optionale Beschreibung"></textarea>
          </app-form-field>

          <div class="form-row">
            <app-form-field label="Distanz (km)" [errorMessage]="getErrorMessage('tourDistance')">
              <input type="number" step="0.1" min="0" formControlName="tourDistance">
            </app-form-field>

            <app-form-field label="Dauer (min)" [errorMessage]="getErrorMessage('estimatedTime')">
              <input type="number" min="0" formControlName="estimatedTime">
            </app-form-field>
          </div>

          <app-form-field label="Tour Bild (Optional)">
            <input type="file" accept="image/*" (change)="onFileSelected($event)">
          </app-form-field>

          @if (selectedFileName) {
            <div class="file-name-display">Ausgewähltes Bild: {{ selectedFileName }}</div>
          }
          @if (!selectedFileName && isEditMode && tour?.tourImagePath) {
            <div class="file-name-display" style="display: flex; align-items: center; justify-content: space-between;">
              <span>Aktuelles Bild: {{ getFileName(tour?.tourImagePath) }}</span>
              <button type="button" class="btn-delete-image" (click)="onDeleteImage()" style="margin-left: 10px; background-color: #dc3545; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer;">Bild löschen</button>
            </div>
          }

          <div class="modal-actions">
            <button type="button" class="btn-cancel" (click)="onCancel()">Abbruch</button>
            <button type="submit" class="btn-submit" [disabled]="tourForm.invalid">Speichern</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styleUrls: ['./tour-form.scss']
})
export class TourFormComponent implements OnInit {
  @Input() tour: Tour | null = null;

  @Output() saved = new EventEmitter<{ request: TourRequest, imageFile: File | null }>();
  @Output() cancelled = new EventEmitter<void>();

  private tourService = inject(TourService);

  tourForm!: FormGroup;
  isEditMode = false;

  selectedImageFile: File | null = null;
  selectedFileName: string | null = null;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.isEditMode = !!this.tour;
    this.initForm();
  }

  private initForm(): void {
    this.tourForm = this.fb.group({
      name: [this.tour?.name || '', [Validators.required, Validators.minLength(3)]],
      fromLocation: [this.tour?.fromLocation || '', Validators.required],
      toLocation: [this.tour?.toLocation || '', Validators.required],
      transportType: [this.tour?.transportType || '', Validators.required],
      description: [this.tour?.description || ''],
      tourDistance: [this.tour?.tourDistance || null, [Validators.min(0)]],
      estimatedTime: [this.tour?.estimatedTime || null, [Validators.min(0)]]
    });
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.tourForm.get(controlName);
    if (!control || !control.errors || !control.touched) return null;

    if (control.errors['required']) return 'Dieses Feld ist erforderlich';
    if (control.errors['minlength']) return `Die Mindestlänge beträgt ${control.errors['minlength'].requiredLength} Zeichen`;
    if (control.errors['min']) return 'Wert muss positiv sein';

    return 'Ungültige Eingabe';
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedImageFile = file;
      this.selectedFileName = file.name;
    } else {
      this.selectedImageFile = null;
      this.selectedFileName = null;
    }
  }

  getFileName(path: string | null | undefined): string {
    if (!path) return '';
    const segments = path.split(/[/\\]/);
    const fileName = segments.pop() || path;
    return fileName.split('?')[0];
  }

  onDeleteImage(): void {
    if (this.tour && this.tour.id) {
      if (confirm('Sind Sie sicher, dass Sie das Bild unwiderruflich löschen möchten?')) {
        this.tourService.deleteTourImage(this.tour.id);
        this.tour.tourImagePath = null;
      }
    }
  }

  onSubmit(): void {
    if (this.tourForm.valid) {
      this.saved.emit({
        request: this.tourForm.value as TourRequest,
        imageFile: this.selectedImageFile
      });
    } else {
      Object.values(this.tourForm.controls).forEach(control => {
        control.markAsTouched();
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
