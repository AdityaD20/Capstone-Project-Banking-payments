import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddVendorPayload, VendorService } from '../../core/services/vendor.service';
import { finalize } from 'rxjs';
import { NotificationService } from '../../core/services/notification.service'; // 1. Import

@Component({
  selector: 'app-add-vendor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="vendorForm" (ngSubmit)="onSubmit()">
      <div class="modal-header">
        <h5 class="modal-title">Add New Vendor</h5>
        <button type="button" class="btn-close" aria-label="Close" (click)="close.emit()"></button>
      </div>
      <div class="modal-body">
        <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <h6>Vendor Details</h6>
        <div class="row g-3 mb-3">
          <div class="col-12">
            <label class="form-label">Vendor/Company Name</label
            ><input type="text" class="form-control" formControlName="name" />
          </div>
          <div class="col-md-6">
            <label class="form-label">Contact Email</label
            ><input type="email" class="form-control" formControlName="email" />
          </div>
          <div class="col-md-6">
            <label class="form-label">Contact Phone</label
            ><input type="text" class="form-control" formControlName="phone" />
          </div>
        </div>
        <hr />
        <h6>Bank Account Details</h6>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Account Number</label
            ><input type="text" class="form-control" formControlName="accountNumber" />
          </div>
          <div class="col-md-6">
            <label class="form-label">IFSC Code</label
            ><input type="text" class="form-control" formControlName="ifscCode" />
          </div>
          <div class="col-12">
            <label class="form-label">Bank Name</label
            ><input type="text" class="form-control" formControlName="bankName" />
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" (click)="close.emit()">Cancel</button>
        <button type="submit" class="btn btn-primary" [disabled]="isLoading || vendorForm.invalid">
          <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Adding...' : 'Add Vendor' }}
        </button>
      </div>
    </form>
  `,
})
export class AddVendorFormComponent {
  vendorForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() success = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder,
    private vendorService: VendorService,
    private notificationService: NotificationService // 2. Inject
  ) {
    this.vendorForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      accountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      bankName: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.vendorForm.invalid) {
      this.vendorForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;

    const payload: AddVendorPayload = this.vendorForm.value;

    this.vendorService
      .addVendor(payload)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          // 3. Replace alert()
          this.notificationService.show('Vendor added successfully!', 'success');
          this.success.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to add vendor.';
        },
      });
  }
}
