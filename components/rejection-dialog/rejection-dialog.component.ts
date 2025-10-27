import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-rejection-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="rejectionForm" (ngSubmit)="submit()">
      <div class="modal-header">
        <h5 class="modal-title">{{ title }}</h5>
        <button type="button" class="btn-close" (click)="close.emit()"></button>
      </div>
      <div class="modal-body">
        <p>{{ prompt }}</p>
        <textarea class="form-control" formControlName="reason" rows="4" placeholder="Enter reason here..."></textarea>
        <div *ngIf="rejectionForm.controls['reason'].touched && rejectionForm.controls['reason'].invalid" class="text-danger small mt-1">
          A reason is required.
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" (click)="close.emit()">Cancel</button>
        <button type="submit" class="btn btn-danger" [disabled]="rejectionForm.invalid">Submit Rejection</button>
      </div>
    </form>
  `
})
export class RejectionDialogComponent {
  @Input() title = 'Confirm Rejection';
  @Input() prompt = 'Please provide a reason for this rejection:';
  @Output() close = new EventEmitter<void>();
  @Output() submitReason = new EventEmitter<string>();

  rejectionForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.rejectionForm = this.fb.group({
      reason: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.rejectionForm.valid) {
      this.submitReason.emit(this.rejectionForm.value.reason);
    } else {
        this.rejectionForm.markAllAsTouched();
    }
  }
}