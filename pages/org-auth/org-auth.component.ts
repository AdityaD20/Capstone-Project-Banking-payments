import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { OrgLoginForm } from '../../components/org-login-form/org-login-form.component';
import { OrgRegisterForm } from '../../components/org-register-form/org-register-form.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-org-auth',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    OrgLoginForm,
    OrgRegisterForm
  ],
  templateUrl: './org-auth.component.html',
  styleUrls: ['./org-auth.component.css']
})
export class OrgAuthComponent {
  // This property now defaults to 'register'
  activeTab: 'login' | 'register' = 'register';
}