import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Employee, EmployeeService } from '../../../core/services/employee.service';
import { Observable, catchError, of, map } from 'rxjs';
import { AddEmployeeFormComponent } from '../../../components/add-employee-form/add-employee-form.component';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { FormsModule } from '@angular/forms'; // 1. IMPORT FormsModule
import { EmployeeFilterPipe } from '../../../pipes/employee-filter.pipe'; // 2. IMPORT THE NEW PIPE

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [
    CommonModule, 
    AddEmployeeFormComponent, 
    TitleCasePipe, 
    RouterLink,
    FormsModule,          // 3. ADD FormsModule
    EmployeeFilterPipe    // 4. ADD EmployeeFilterPipe
  ],
  templateUrl: './employees.component.html',
  styleUrls: ['./employees.component.css']
})
export class EmployeesComponent implements OnInit {
  pendingApprovalEmployees$!: Observable<Employee[]>;
  allOtherEmployees$!: Observable<Employee[]>;
  errorMessage: string | null = null;
  batchUploadMessage: { type: 'success' | 'danger', text: string } | null = null;

  isAddEmployeeModalVisible = false;
  isUploadingBatch = false;
  selectedFile: File | null = null;

  public searchTerm: string = ''; // 5. ADD a property for the search term

  constructor(
    private employeeService: EmployeeService, 
    private router: Router,
    private confirmationService: ConfirmationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.errorMessage = null;
    const allEmployees$ = this.employeeService.fetchAndLoadEmployees().pipe(
      catchError(err => {
        this.errorMessage = 'Could not load employees. Please try again later.';
        return of([]);
      })
    );
    this.pendingApprovalEmployees$ = allEmployees$.pipe(
      map(employees => employees.filter(emp => emp.status === 'PENDING_APPROVAL'))
    );
    this.allOtherEmployees$ = allEmployees$.pipe(
      map(employees => employees.filter(emp => emp.status !== 'PENDING_APPROVAL'))
    );
  }

  openAddEmployeeModal(): void { this.isAddEmployeeModalVisible = true; }
  closeAddEmployeeModal(): void { this.isAddEmployeeModalVisible = false; }
  onEmployeeAdded(): void {
    this.closeAddEmployeeModal();
    // No manual refresh needed due to reactive service
  }

  navigateToActivation(employee: Employee): void {
    this.router.navigate(['/organization/employees/activate', employee.id]);
  }

  disableEmployee(employee: Employee): void {
    this.confirmationService.confirm({
      title: 'Confirm Disable',
      message: `Are you sure you want to disable ${employee.firstName} ${employee.lastName}?`,
      confirmText: 'Disable',
      isDestructive: true
    }).subscribe(result => {
      if (result) {
        this.employeeService.softDeleteEmployee(employee.id).subscribe({
            next: () => this.notificationService.show('Employee disabled successfully.', 'success'),
            error: (err) => this.notificationService.show(`Error: ${err.error?.message}`, 'danger')
        });
      }
    });
  }

  enableEmployee(employee: Employee): void {
    this.confirmationService.confirm({
      title: 'Confirm Enable',
      message: `Re-enable ${employee.firstName}?`,
      confirmText: 'Enable'
    }).subscribe(result => {
      if(result) {
        this.employeeService.enableEmployee(employee.id).subscribe({
            next: () => this.notificationService.show('Employee enabled successfully.', 'success'),
            error: (err) => this.notificationService.show(`Error: ${err.error?.message}`, 'danger')
        });
      }
    });
  }

  onFileSelected(event: any): void { this.selectedFile = event.target.files[0] ?? null; }
  onBatchUpload(): void {
    if (!this.selectedFile) return;
    this.isUploadingBatch = true;
    this.batchUploadMessage = null;
    this.employeeService.batchUploadEmployees(this.selectedFile).subscribe({
        next: (response) => {
          this.isUploadingBatch = false;
          this.batchUploadMessage = { type: 'success', text: response };
          this.employeeService.fetchAndLoadEmployees().subscribe();
          this.selectedFile = null;
          const fileInput = document.getElementById('batchFile') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
        },
        error: (err) => {
          this.isUploadingBatch = false;
          this.batchUploadMessage = { type: 'danger', text: err.error?.message || 'Batch upload failed.' };
        }
    });
  }
}