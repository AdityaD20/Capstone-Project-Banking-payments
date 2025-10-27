import { Component,ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';
import { NgxCaptchaModule, ReCaptcha2Component } from 'ngx-captcha';
import { environment } from '../../environments/environment'; // 1. IMPORT environment

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgxCaptchaModule],
  template: `
    <div class="container-fluid vh-100 d-flex justify-content-center align-items-center bg-light">
      <div class="card shadow-sm" style="width: 100%; max-width: 400px;">
        <div class="card-body p-4">
          <h3 class="card-title text-center mb-4">Login</h3>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <div *ngIf="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <div class="mb-3">
              <label for="email" class="form-label">Email Address</label>
              <input type="email" id="email" class="form-control" formControlName="email">
            </div>

            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <input type="password" id="password" class="form-control" formControlName="password">
            </div>

            <div class="text-end mb-3">
              <a routerLink="/forgot-password" class="text-secondary small">Forgot Password?</a>
            </div>

            <div class="mb-3 d-flex justify-content-center">
              <ngx-recaptcha2 
                #captchaElem 
                [siteKey]="siteKey" 
                formControlName="captchaResponse">
              </ngx-recaptcha2>
            </div>

            <div class="d-grid mt-4">
              <button class="btn btn-primary" type="submit" [disabled]="isLoading || loginForm.invalid">
                <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                {{ isLoading ? 'Logging in...' : 'Login' }}
              </button>
            </div>
            <div class="text-center mt-3">
              <a routerLink="/" class="text-secondary small">Back to Home</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  siteKey: string; // 2. ADD a property for the site key

   @ViewChild('captchaElem') captchaElem!: ReCaptcha2Component;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.siteKey = environment.recaptcha.siteKey; // 2. INITIALIZE the site key
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      captchaResponse: [null, Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }
    this.isLoading = true;
    this.authService.login(this.loginForm.value)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (res) => {
          if (res.roles.includes('ROLE_BANK_ADMIN')) {
            this.router.navigate(['/admin/dashboard']);
          } else if (res.roles.includes('ROLE_EMPLOYEE')) {
            this.router.navigate(['/employee/dashboard']);
          } else {
            this.errorMessage = 'You do not have permission to log in here.';
            this.authService.logout(false);
            this.captchaElem.reloadCaptcha();
          }
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Login Failed. Please check your credentials.';
          this.captchaElem.reloadCaptcha();
        },
      });
  }
}