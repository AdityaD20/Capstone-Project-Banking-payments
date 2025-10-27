import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddEmployeePayload, EmployeeService } from '../../core/services/employee.service';
import { finalize } from 'rxjs';
import { NotificationService } from '../../core/services/notification.service'; // 1. Import

@Component({
  selector: 'app-add-employee-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="employeeForm" (ngSubmit)="onSubmit()">
      <div class="modal-header">
        <h5 class="modal-title">Add New Employee</h5>
        <button type="button" class="btn-close" aria-label="Close" (click)="close.emit()"></button>
      </div>
      <div class="modal-body">
        <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="firstName" class="form-label">First Name</label
            ><input type="text" id="firstName" class="form-control" formControlName="firstName" />
          </div>
          <div class="col-md-6 mb-3">
            <label for="lastName" class="form-label">Last Name</label
            ><input type="text" id="lastName" class="form-control" formControlName="lastName" />
          </div>
        </div>
        <div class="mb-3">
          <label for="email" class="form-label">Email Address</label
          ><input type="email" id="email" class="form-control" formControlName="email" />
        </div>
        <div class="mb-3">
          <label for="dateOfBirth" class="form-label">Date of Birth</label
          ><input type="date" id="dateOfBirth" class="form-control" formControlName="dateOfBirth" />
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" (click)="close.emit()">Cancel</button>
        <button
          type="submit"
          class="btn btn-primary"
          [disabled]="isLoading || employeeForm.invalid"
        >
          <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Adding...' : 'Add Employee' }}
        </button>
      </div>
    </form>
  `,
})
export class AddEmployeeFormComponent {
  employeeForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() success = new EventEmitter<void>();

  constructor(
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private notificationService: NotificationService // 2. Inject
  ) {
    this.employeeForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      dateOfBirth: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    const payload: AddEmployeePayload = this.employeeForm.value;
    this.employeeService
      .addEmployee(payload)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          // 3. Replace alert()
          this.notificationService.show('Employee added successfully!', 'success');
          this.success.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to add employee.';
        },
      });
  }
}
