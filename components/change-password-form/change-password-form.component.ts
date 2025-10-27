import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ChangePasswordPayload, UserService } from '../../core/services/user.service';
import { finalize } from 'rxjs';
import { passwordMatchValidator } from '../../validators/password-match.validator';

@Component({
  selector: 'app-change-password-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="passwordForm" (ngSubmit)="onSubmit()">
      <div class="modal-header">
        <h5 class="modal-title">Change Your Password</h5>
        <!-- No close button to force the action -->
      </div>
      <div class="modal-body">
        <p class="text-muted">For your security, you must change your temporary password before proceeding.</p>
        <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

        <div class="mb-3">
          <label for="oldPassword" class="form-label">Old (Temporary) Password</label>
          <input type="password" id="oldPassword" class="form-control" formControlName="oldPassword">
        </div>

        <div class="mb-3">
          <label for="newPassword" class="form-label">New Password</label>
          <input type="password" id="newPassword" class="form-control" formControlName="newPassword">
            <div *ngIf="passwordForm.controls['newPassword'].touched && passwordForm.controls['newPassword'].errors" class="text-danger small mt-1">
                <div *ngIf="passwordForm.controls['newPassword'].errors['required']">New password is required.</div>
                <div *ngIf="passwordForm.controls['newPassword'].errors['pattern']">
                    Must be 8+ characters and include uppercase, lowercase, a number, and a special symbol.
                </div>
            </div>          
        </div>

        <div class="mb-3">
          <label for="confirmNewPassword" class="form-label">Re-enter New Password</label>
          <input type="password" id="confirmNewPassword" class="form-control" formControlName="confirmNewPassword">
          <div *ngIf="passwordForm.controls['confirmNewPassword'].touched && passwordForm.controls['confirmNewPassword'].errors" class="text-danger small mt-1">
            <div *ngIf="passwordForm.controls['confirmNewPassword'].errors['required']">Please confirm your new password.</div>
            <div *ngIf="passwordForm.controls['confirmNewPassword'].errors['passwordMismatch']">Passwords do not match.</div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn btn-primary w-100" [disabled]="isLoading || passwordForm.invalid">
          <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2"></span>
          {{ isLoading ? 'Saving...' : 'Save and Continue' }}
        </button>
      </div>
    </form>
  `
})
export class ChangePasswordFormComponent {
  passwordForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;

  @Output() success = new EventEmitter<void>();

  constructor(private fb: FormBuilder, private userService: UserService) {
    this.passwordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [
                Validators.required,
                Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$')
            ]],
      confirmNewPassword: ['', Validators.required]
    }, {
      validators: passwordMatchValidator('newPassword', 'confirmNewPassword') // <-- APPLY VALIDATOR
    });
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;

    const payload: ChangePasswordPayload = this.passwordForm.value;

    this.userService.changePassword(payload)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.success.emit(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to change password. Please check your old password.';
        }
      });
  }
}