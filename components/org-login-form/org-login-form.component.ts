import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { finalize } from 'rxjs';
import { NgxCaptchaModule, ReCaptcha2Component } from 'ngx-captcha';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-org-login-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NgxCaptchaModule
  ],
  templateUrl: './org-login-form.component.html',
  styleUrls: ['./org-login-form.component.css']
})
export class OrgLoginForm {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  siteKey: string; // Added

  @ViewChild('captchaElem') captchaElem!: ReCaptcha2Component;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.siteKey = environment.recaptcha.siteKey; // Added
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      captchaResponse: [null, Validators.required] // Added
    });
  }

  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    this.errorMessage = null;
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.authService.login(this.loginForm.value)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          const userRoles = response.roles;
          if (userRoles.includes('ROLE_ORGANIZATION')) {
            this.router.navigate(['/organization/dashboard']);
          } else {
            this.errorMessage = 'You do not have permission to access the organization portal.';
            this.authService.logout(false);
            this.captchaElem.reloadCaptcha();
          }
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Invalid email or password.';
          this.captchaElem.reloadCaptcha();
        }
      });
  }
}