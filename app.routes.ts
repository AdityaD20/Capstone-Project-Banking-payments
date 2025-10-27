import { Routes } from '@angular/router';
import { LandingComponent } from './pages/landing/landing.component';
import { OrgAuthComponent } from './pages/org-auth/org-auth.component';
import { DashboardLayoutComponent } from './layouts/dashboard/dashboard.component';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './components/login-form/login-form.component';
import { AdminDashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { OrganizationDashboardComponent } from './pages/organization/dashboard/dashboard.component';
import { EmployeesComponent } from './pages/organization/employees/employees.component';
import { ActivateEmployeeComponent } from './pages/organization/activate-employee/activate-employee.component';
import { PayrollComponent } from './pages/organization/payroll/payroll.component';
import { CreatePaymentComponent } from './pages/organization/create-payment/create-payment.component';
import { ReportsComponent } from './pages/organization/reports/reports.component';
import { ConcernsComponent } from './pages/organization/concerns/concerns.component';
import { VendorsComponent } from './pages/organization/vendors/vendors.component';
import { DepositsComponent } from './pages/organization/deposits/deposits.component';
import { EmployeeDashboardComponent } from './pages/employee/dashboard/dashboard.component';
import { MyConcernsComponent } from './pages/employee/my-concerns/my-concerns.component';
import { EmployeeDetailComponent } from './pages/organization/employee-detail/employee-detail.component';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { OrganizationsComponent } from './pages/admin/organizations/organizations.component';
import { PendingApprovalsComponent } from './pages/admin/pending-approvals/pending-approvals.component';
import { PaymentRequestsComponent } from './pages/admin/payment-requests/payment-requests.component';
import { DepositRequestsComponent } from './pages/admin/deposit-requests/deposit-requests.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'organization/auth', component: OrgAuthComponent },
  { path: 'login', component: LoginComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  {
    path: 'admin',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'organizations', component: OrganizationsComponent },
      // 2. ADD NEW ADMIN ROUTES
      { path: 'pending-approvals', component: PendingApprovalsComponent },
      { path: 'payment-requests', component: PaymentRequestsComponent },
      { path: 'deposit-requests', component: DepositRequestsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  {
    path: 'organization',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: OrganizationDashboardComponent },
      { path: 'employees', component: EmployeesComponent },
      { path: 'employees/activate/:id', component: ActivateEmployeeComponent },
      { path: 'employees/:id', component: EmployeeDetailComponent },
      { path: 'payroll', component: PayrollComponent },
      { path: 'create-payment', component: CreatePaymentComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'concerns', component: ConcernsComponent },
      { path: 'vendors', component: VendorsComponent },
      { path: 'deposits', component: DepositsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  {
    path: 'employee',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: EmployeeDashboardComponent },
      { path: 'my-concerns', component: MyConcernsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: '' },
];