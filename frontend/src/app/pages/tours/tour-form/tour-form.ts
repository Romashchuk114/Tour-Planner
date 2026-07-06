import { Component, EventEmitter, Input, Output, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Tour, TourRequest } from '../../../models/tour.model';
import { TRANSPORT_OPTIONS } from '../../../models/transport-types';
import { FormFieldComponent } from '../../../components/form-field/form-field.component';
import {
  LocationAutocompleteComponent,
  LocationSuggestion
} from '../../../components/location-autocomplete/location-autocomplete.component';
import { TourService } from '../../../services/tour.service';

const MAX_STAGES = 9;

@Component({
  selector: 'app-tour-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormFieldComponent, LocationAutocompleteComponent],
  template: `
    <div class="modal-overlay" (click)="onCancel()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h2>{{ isEditMode ? 'Tour Bearbeiten' : 'Neue Tour' }}</h2>

        <form [formGroup]="tourForm" (ngSubmit)="onSubmit()">

          <app-form-field label="Name" [errorMessage]="getErrorMessage('name')">
            <input type="text" formControlName="name" placeholder="Tour name">
          </app-form-field>

          <app-form-field label="Von" [errorMessage]="getFromError()">
            <app-location-autocomplete
              placeholder="Startort suchen…"
              [initialValue]="initialFrom"
              (selectedChange)="onFromSelected($event)">
            </app-location-autocomplete>
          </app-form-field>

          @for (stageGroup of stagesArray.controls; track stageGroup; let i = $index, last = $last) {
            @if (last) {
              <button type="button" class="btn-add-stop"
                      (click)="addStop()"
                      [disabled]="stagesArray.length >= maxStages">
                + Zwischenstopp hinzufügen
                @if (stagesArray.length >= maxStages) {
                  <span class="cap-hint"> (max. {{ maxStages - 1 }} Stopps)</span>
                }
              </button>
            }
            <div class="stage-block">
              <app-form-field [label]="'Transport (Etappe ' + (i + 1) + ')'" [errorMessage]="getStageTransportError(i)">
                <select [formControl]="stageTransportControl(i)">
                  <option value="" disabled>Wähle Transport Typ</option>
                  @for (opt of transportOptions; track opt.value) {
                    <option [value]="opt.value">{{ opt.label }}</option>
                  }
                </select>
              </app-form-field>

              <div class="endpoint-row">
                <app-form-field
                  [label]="last ? 'Nach' : 'Zwischenstopp ' + (i + 1)"
                  [errorMessage]="getStageEndpointError(i)">
                  <app-location-autocomplete
                    [placeholder]="last ? 'Zielort suchen…' : 'Stopp suchen…'"
                    [initialValue]="initialEndpoints[i] ?? null"
                    (selectedChange)="onStageEndpointSelected(i, $event)">
                  </app-location-autocomplete>
                </app-form-field>
                @if (!last) {
                  <button type="button" class="btn-remove-stop" (click)="removeStop(i)">
                    Entfernen
                  </button>
                }
              </div>
            </div>
          }

          <app-form-field label="Beschreibung" [errorMessage]="getErrorMessage('description')">
            <textarea formControlName="description" rows="3" placeholder="Optionale Beschreibung"></textarea>
          </app-form-field>

          <p class="info">Distanz und Dauer werden pro Etappe automatisch über OpenRouteService berechnet.</p>

          <app-form-field label="Tour Bild (Optional)">
            <input type="file" accept="image/*" (change)="onFileSelected($event)">
          </app-form-field>

          @if (selectedFileName) {
            <div class="file-name-display">Ausgewähltes Bild: {{ selectedFileName }}</div>
          }
          @if (!selectedFileName && isEditMode && tour?.tourImagePath) {
            <div class="file-name-display existing-image">
              <span>Aktuelles Bild: {{ getFileName(tour?.tourImagePath) }}</span>
              <button type="button" class="btn-delete-image" (click)="onDeleteImage()">Bild löschen</button>
            </div>
          }

          <div class="modal-actions">
            <button type="button" class="btn-cancel" (click)="onCancel()">Abbruch</button>
            <button type="submit" class="btn-submit" [disabled]="!tourForm.valid">Speichern</button>
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

  private fb = inject(FormBuilder);
  private tourService = inject(TourService);

  readonly transportOptions = TRANSPORT_OPTIONS;
  readonly maxStages = MAX_STAGES;

  tourForm!: FormGroup;
  isEditMode = false;

  selectedImageFile: File | null = null;
  selectedFileName: string | null = null;

  initialFrom: LocationSuggestion | null = null;
  initialEndpoints: (LocationSuggestion | null)[] = [];

  ngOnInit(): void {
    this.isEditMode = !!this.tour;

    if (this.tour) {
      this.initialFrom = {
        name: this.tour.fromName,
        lat: this.tour.fromLat,
        lng: this.tour.fromLng
      };
      this.initialEndpoints = this.tour.stages.map(s => ({
        name: s.endName, lat: s.endLat, lng: s.endLng
      }));
    } else {
      this.initialEndpoints = [null];
    }

    this.initForm();
  }

  private initForm(): void {
    const stageGroups = this.tour?.stages?.length
      ? this.tour.stages.map(s => this.buildStageGroup(s.transportType, s.endName, s.endLat, s.endLng))
      : [this.buildStageGroup('', '', null, null)];

    this.tourForm = this.fb.group({
      name: [this.tour?.name || '', [Validators.required, Validators.minLength(3)]],
      description: [this.tour?.description || ''],
      from: this.fb.group({
        name: [this.tour?.fromName ?? '', Validators.required],
        lat: [this.tour?.fromLat ?? null, Validators.required],
        lng: [this.tour?.fromLng ?? null, Validators.required]
      }),
      stages: this.fb.array(stageGroups)
    });
  }

  private buildStageGroup(transportType: string, endName: string, endLat: number | null, endLng: number | null): FormGroup {
    return this.fb.group({
      transportType: [transportType, Validators.required],
      endName: [endName, Validators.required],
      endLat: [endLat, Validators.required],
      endLng: [endLng, Validators.required]
    });
  }

  get stagesArray(): FormArray<FormGroup> {
    return this.tourForm.get('stages') as FormArray<FormGroup>;
  }

  stageTransportControl(i: number): FormControl {
    return this.stagesArray.at(i).get('transportType') as FormControl;
  }

  addStop(): void {
    if (this.stagesArray.length >= this.maxStages) return;
    // Insert vor letztem Stage (= Ziel) damit das Ziel hinten bleibt.
    const insertIndex = this.stagesArray.length - 1;
    const lastTransport = this.stageTransportControl(insertIndex).value ?? '';
    this.stagesArray.insert(insertIndex, this.buildStageGroup(lastTransport, '', null, null));
    this.initialEndpoints.splice(insertIndex, 0, null);
  }

  removeStop(index: number): void {
    if (index === this.stagesArray.length - 1) return; // Ziel nicht entfernbar
    this.stagesArray.removeAt(index);
    this.initialEndpoints.splice(index, 1);
  }

  onFromSelected(s: LocationSuggestion | null): void {
    this.tourForm.get('from')!.patchValue(s ?? { name: '', lat: null, lng: null });
  }

  onStageEndpointSelected(i: number, s: LocationSuggestion | null): void {
    this.stagesArray.at(i).patchValue(s
      ? { endName: s.name, endLat: s.lat, endLng: s.lng }
      : { endName: '', endLat: null, endLng: null });
  }

  getFromError(): string | null {
    const from = this.tourForm?.get('from');
    if (!from || !from.touched || from.valid) return null;
    return 'Bitte einen Startort aus der Liste wählen';
  }

  getStageEndpointError(i: number): string | null {
    const group = this.stagesArray.at(i);
    if (!group.touched) return null;
    const isLast = i === this.stagesArray.length - 1;
    const endInvalid = group.get('endName')?.invalid || group.get('endLat')?.invalid;
    if (!endInvalid) return null;
    return isLast ? 'Bitte einen Zielort aus der Liste wählen' : 'Bitte einen Stopp aus der Liste wählen';
  }

  getStageTransportError(i: number): string | null {
    const ctrl = this.stageTransportControl(i);
    if (!ctrl.touched || ctrl.valid) return null;
    return 'Bitte Transport Typ wählen';
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.tourForm.get(controlName);
    if (!control || !control.errors || !control.touched) return null;
    if (control.errors['required']) return 'Dieses Feld ist erforderlich';
    if (control.errors['minlength']) return `Die Mindestlänge beträgt ${control.errors['minlength'].requiredLength} Zeichen`;
    return 'Ungültige Eingabe';
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.selectedImageFile = file;
    this.selectedFileName = file?.name ?? null;
  }

  getFileName(path: string | null | undefined): string {
    if (!path) return '';
    const segments = path.split(/[/\\]/);
    const fileName = segments.pop() || path;
    return fileName.split('?')[0];
  }

  onDeleteImage(): void {
    if (!this.tour?.id) return;
    if (confirm('Sind Sie sicher, dass Sie das Bild unwiderruflich löschen möchten?')) {
      this.tourService.deleteTourImage(this.tour.id);
      this.tour.tourImagePath = null;
    }
  }

  onSubmit(): void {
    if (this.tourForm.invalid) {
      this.tourForm.markAllAsTouched();
      return;
    }

    const v = this.tourForm.value;
    const request: TourRequest = {
      name: v.name,
      description: v.description,
      fromName: v.from.name,
      fromLat: v.from.lat,
      fromLng: v.from.lng,
      stages: v.stages.map((s: { transportType: string; endName: string; endLat: number; endLng: number }) => ({
        transportType: s.transportType,
        endName: s.endName,
        endLat: s.endLat,
        endLng: s.endLng
      }))
    };

    this.saved.emit({ request, imageFile: this.selectedImageFile });
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
