import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';
import { NgxCaptchaModule } from 'ngx-captcha';
import { environment } from '../../environments/environment';
import { passwordMatchValidator } from '../../validators/password-match.validator';

@Component({
  selector: 'app-org-register-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgxCaptchaModule // Added
  ],
  templateUrl: './org-register-form.component.html',
  styleUrls: ['./org-register-form.component.css']
})
export class OrgRegisterForm {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  siteKey: string; // Added

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.siteKey = environment.recaptcha.siteKey; // Added
    this.registerForm = this.fb.group({
      organizationName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
                Validators.required, 
                Validators.pattern('^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$')
            ]],
      confirmPassword: ['', Validators.required],
      captchaResponse: [null, Validators.required],
    }, {
      validators: passwordMatchValidator('password', 'confirmPassword') // <-- APPLY VALIDATOR
    });
  }

  get f() { return this.registerForm.controls; }

  onSubmit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    // The form value now includes the captchaResponse, which the service will send
    this.authService.registerOrganization(this.registerForm.value)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.successMessage = response.message;
          this.registerForm.reset();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        }
      });
  }
}