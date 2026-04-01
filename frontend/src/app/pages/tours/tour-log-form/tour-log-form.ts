import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TourLog, TourLogRequest } from '../../../models/tour-log.model';
import { FormFieldComponent } from '../../../components/form-field/form-field.component';

@Component({
  selector: 'app-tour-log-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormFieldComponent],
  template: `
    <div class="modal-overlay" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>{{ isEditMode ? 'Tour Log Bearbeiten' : 'Neues Tour Log' }}</h2>

        <form [formGroup]="logForm" (ngSubmit)="onSubmit()">

          <app-form-field label="Datum und Zeit" [errorMessage]="getErrorMessage('dateTime')">
            <input type="datetime-local" formControlName="dateTime">
          </app-form-field>

          <div class="form-row">
            <app-form-field label="Schwierigkeit (1-10)" [errorMessage]="getErrorMessage('difficulty')">
              <input type="number" min="1" max="10" formControlName="difficulty">
            </app-form-field>

            <app-form-field label="Bewertung (1-5)" [errorMessage]="getErrorMessage('rating')">
              <input type="number" min="1" max="5" formControlName="rating">
            </app-form-field>
          </div>

          <div class="form-row">
            <app-form-field label="Distanz (km)" [errorMessage]="getErrorMessage('totalDistance')">
              <input type="number" step="0.1" min="0.1" formControlName="totalDistance">
            </app-form-field>

            <app-form-field label="Dauer (min)" [errorMessage]="getErrorMessage('totalTime')">
              <input type="number" min="1" formControlName="totalTime">
            </app-form-field>
          </div>

          <app-form-field label="Kommentar" [errorMessage]="getErrorMessage('comment')">
            <textarea formControlName="comment" rows="3" placeholder="Optionaler Kommentar"></textarea>
          </app-form-field>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" (click)="onCancel()">Abbrechen</button>
            <button type="submit" class="btn-submit" [disabled]="logForm.invalid">Speichern</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styleUrls: ['./tour-log-form.scss']
})
export class TourLogFormComponent implements OnInit {
  @Input() log: TourLog | null = null;
  @Output() saved = new EventEmitter<TourLogRequest>();
  @Output() cancelled = new EventEmitter<void>();

  logForm!: FormGroup;
  isEditMode = false;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.isEditMode = !!this.log;
    this.initForm();
  }

  private initForm(): void {
    // Format date string to match datetime-local input requirements if editing
    let initialDate;
    if (this.log?.dateTime) {
      // Typically need "YYYY-MM-DDThh:mm" format for input type="datetime-local"
      const dateObj = new Date(this.log.dateTime);
      initialDate = dateObj.toISOString().slice(0, 16);
    } else {
      // Default to now
      initialDate = new Date().toISOString().slice(0, 16);
    }

    this.logForm = this.fb.group({
      dateTime: [initialDate, Validators.required],
      difficulty: [this.log?.difficulty || 5, [Validators.required, Validators.min(1), Validators.max(10)]],
      rating: [this.log?.rating || 3, [Validators.required, Validators.min(1), Validators.max(5)]],
      totalDistance: [this.log?.totalDistance || null, [Validators.required, Validators.min(0.1)]],
      totalTime: [this.log?.totalTime || null, [Validators.required, Validators.min(1)]],
      comment: [this.log?.comment || '']
    });
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.logForm.get(controlName);
    if (!control || !control.errors || !control.touched) return null;

    if (control.errors['required']) return 'This field is required';
    if (control.errors['min']) return `Minimum value is ${control.errors['min'].min}`;
    if (control.errors['max']) return `Maximum value is ${control.errors['max'].max}`;

    return 'Invalid field';
  }

  onSubmit(): void {
    if (this.logForm.valid) {
      // Convert datetime-local string to ISO string before sending (or let backend handle string)
      const formValue = { ...this.logForm.value };

      // Ensure time string conforms to ISO or backend expectations
      if (formValue.dateTime && formValue.dateTime.length === 16) {
         formValue.dateTime = formValue.dateTime + ':00'; // Append seconds if needed
      }

      this.saved.emit(formValue as TourLogRequest);
    } else {
      Object.values(this.logForm.controls).forEach(control => {
        control.markAsTouched();
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
