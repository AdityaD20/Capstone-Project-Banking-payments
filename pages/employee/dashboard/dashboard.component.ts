import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChangePasswordFormComponent } from '../../../components/change-password-form/change-password-form.component';
import { UserDetails, UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { EmployeeDocUploaderComponent } from '../../../components/employee-doc-uploader/employee-doc-uploader.component';
import { Employee, EmployeeService } from '../../../core/services/employee.service';
import { Observable, catchError, of, finalize } from 'rxjs';
import { PayslipListComponent} from '../../../components/payslip-list/payslip-list.component';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, ChangePasswordFormComponent, EmployeeDocUploaderComponent, PayslipListComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {
  // 1. NEW STATE VARIABLE
  public viewState: 'loading' | 'passwordChange' | 'dashboard' = 'loading';
  
  userDetails: UserDetails | null = null;
  employeeDetails$: Observable<Employee | null> | null = null;
  errorMessage: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private employeeService: EmployeeService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.viewState = 'loading'; // Always start in the loading state
    this.errorMessage = null;

    this.userService.getMe().subscribe({
      next: (details) => {
        this.userDetails = details;
        
        if (details.passwordChangeRequired) {
          // If password change is needed, we have all the data we need.
          // Switch the view state directly.
          this.viewState = 'passwordChange';
        } else {
          // If password is fine, now we need to fetch the employee profile.
          // The dashboard will remain in the 'loading' state until this second call completes.
          this.loadEmployeeDetails();
        }
      },
      error: (err) => {
        this.errorMessage = "Could not load your user details. Please try logging in again.";
        this.viewState = 'dashboard'; // Show dashboard to display the error message
      }
    });
  }

  loadEmployeeDetails(): void {
    this.employeeDetails$ = this.employeeService.getMe().pipe(
      catchError(err => {
        this.errorMessage = "Could not load your employee profile. Please contact your administrator.";
        return of(null);
      })
    );
    // After the employee details have been fetched (or failed), switch to the dashboard view.
    this.viewState = 'dashboard';
  }

  onPasswordChanged(): void {
    this.notificationService.show("Password changed successfully! You will now be logged out. Please log in again.", 'success');
    this.viewState = 'loading'; // Show loading spinner before logout
    
    setTimeout(() => {
      this.authService.logout();
    }, 2500);
  }

  // This method is called by the uploader component after a successful upload
  onUploadSuccess(): void {
    // Show a loading state briefly while we re-fetch the employee's new status
    this.viewState = 'loading';
    this.loadEmployeeDetails();
  }
}