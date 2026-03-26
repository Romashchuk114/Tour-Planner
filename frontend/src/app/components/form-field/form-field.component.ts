import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [CommonModule],
  template: `
    <label class="field">
      <span class="label">{{ label() }}</span>
      <ng-content />
      @if (errorMessage()) {
        <span class="field-error">{{ errorMessage() }}</span>
      }
    </label>
  `,
  styleUrls: ['./form-field.component.scss']
})
export class FormFieldComponent {
  label = input.required<string>();
  errorMessage = input<string | null>(null);
}
