import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

export interface LocationSuggestion {
  name: string;
  lat: number;
  lng: number;
}

interface NominatimResult {
  display_name: string;
  lat: string;
  lon: string;
}

@Component({
  selector: 'app-location-autocomplete',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="autocomplete">
      <input
        type="text"
        [(ngModel)]="query"
        (ngModelChange)="onInput($event)"
        (focus)="showDropdown = true"
        (blur)="onBlur()"
        [placeholder]="placeholder"
        [class.invalid]="touched && !selected"
        autocomplete="off"
      />
      @if (showDropdown && (loading || suggestions.length > 0)) {
        <ul class="dropdown">
          @if (loading) {
            <li class="loading">Suche…</li>
          }
          @for (s of suggestions; track s.name) {
            <li (mousedown)="select(s)">{{ s.name }}</li>
          }
        </ul>
      }
      @if (touched && !selected && query.length > 0) {
        <span class="hint">Bitte einen Vorschlag aus der Liste wählen</span>
      }
    </div>
  `,
  styleUrls: ['./location-autocomplete.component.scss']
})
export class LocationAutocompleteComponent implements OnInit, OnDestroy {
  @Input() placeholder = 'Ort suchen…';
  @Input() initialValue: LocationSuggestion | null = null;

  @Output() selectedChange = new EventEmitter<LocationSuggestion | null>();

  private http = inject(HttpClient);

  query = '';
  suggestions: LocationSuggestion[] = [];
  selected: LocationSuggestion | null = null;
  loading = false;
  showDropdown = false;
  touched = false;

  private input$ = new Subject<string>();
  private sub?: Subscription;

  ngOnInit(): void {
    if (this.initialValue) {
      this.selected = this.initialValue;
      this.query = this.initialValue.name;
    }

    this.sub = this.input$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (q.length < 3) {
          this.suggestions = [];
          this.loading = false;
          return [];
        }
        this.loading = true;
        const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(q)}&format=json&limit=5&addressdetails=0`;
        return this.http.get<NominatimResult[]>(url);
      })
    ).subscribe({
      next: (results) => {
        this.loading = false;
        this.suggestions = (results as NominatimResult[]).map(r => ({
          name: r.display_name,
          lat: parseFloat(r.lat),
          lng: parseFloat(r.lon)
        }));
      },
      error: () => {
        this.loading = false;
        this.suggestions = [];
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  onInput(value: string): void {
    this.touched = true;
    this.showDropdown = true;
    if (this.selected && this.selected.name !== value) {
      this.selected = null;
      this.selectedChange.emit(null);
    }
    this.input$.next(value);
  }

  onBlur(): void {
    setTimeout(() => { this.showDropdown = false; }, 150);
    this.touched = true;
  }

  select(s: LocationSuggestion): void {
    this.selected = s;
    this.query = s.name;
    this.suggestions = [];
    this.showDropdown = false;
    this.selectedChange.emit(s);
  }
}
