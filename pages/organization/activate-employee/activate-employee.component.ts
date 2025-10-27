import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiDocument, Employee, EmployeeFinalizePayload, EmployeeService } from '../../../core/services/employee.service';
import { finalize, Observable, take, Subscription } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { RejectionDialogComponent } from '../../../components/rejection-dialog/rejection-dialog.component';

// Note: This component doesn't have the rejection dialog component. For a real text prompt,
// we would create a new modal. For now, we will replace prompt() with a confirm() and a hardcoded reason,
// but the ideal solution would be a reusable text input modal.

@Component({
  selector: 'app-activate-employee',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RejectionDialogComponent],
  templateUrl: './activate-employee.component.html',
  styleUrl: './activate-employee.component.css'
})
export class ActivateEmployeeComponent implements OnInit, OnDestroy {
  employee: Employee | null = null;
  documents$!: Observable<ApiDocument[]>;
  activationForm: FormGroup;
  private salarySub!: Subscription;

  isLoading = true;
  errorMessage: string | null = null;
  isRejectionModalVisible = false;

  isActivating = false;
  isRejecting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private employeeService: EmployeeService,
    private notificationService: NotificationService, // Injected
    private confirmationService: ConfirmationService // Injected
  ) {
    this.activationForm = this.fb.group({
      employeeNumber: ['', Validators.required],
      accountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      bankName: ['', Validators.required],
      basicSalary: [0, [Validators.required, Validators.min(0)]],
      hra: [{ value: 0 }],
      da: [{ value: 0}],
      pf: [{ value: 0}],
      otherAllowances:[{ value: 0 }],
    });
  }

  ngOnInit(): void {
    this.salarySub = this.activationForm.get('basicSalary')!.valueChanges.subscribe(basic => {
      if (typeof basic === 'number') {
        const hra = parseFloat((basic * 0.40).toFixed(2));
        const da = parseFloat((basic * 0.15).toFixed(2));
        const pf = parseFloat((basic * 0.12).toFixed(2));
        const otherAllowances = parseFloat((basic * 0.10).toFixed(2));


        this.activationForm.patchValue({ hra, da, pf, otherAllowances }, { emitEvent: false });
      }
    });

    const employeeId = Number(this.route.snapshot.paramMap.get('id'));
    if (!employeeId) {
      this.errorMessage = "Invalid employee ID provided in URL.";
      this.isLoading = false;
      return;
    }

    this.employeeService.getEmployeeById(employeeId).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: (emp) => {
        this.employee = emp;
        if (this.employee.status !== 'PENDING_APPROVAL') {
          this.errorMessage = "This employee is not currently pending document approval.";
          this.activationForm.disable();
        }
        this.documents$ = this.employeeService.getEmployeeDocuments(this.employee.id);
      },
      error: (err) => this.errorMessage = "Employee not found."
    });
  }

  onActivate(): void {
    if (this.activationForm.invalid || !this.employee) {
      this.activationForm.markAllAsTouched();
      return;
    }

    this.isActivating = true;
    this.errorMessage = null;
    const payload: EmployeeFinalizePayload = this.activationForm.value;

    this.employeeService.activateEmployee(this.employee.id, payload)
      .pipe(finalize(() => this.isActivating = false))
      .subscribe({
        next: () => {
          // REPLACED alert()
          this.notificationService.show('Employee activated successfully!', 'success');
          this.router.navigate(['/organization/employees']);
        },
        error: (err) => this.errorMessage = err.error?.message || 'Activation failed.'
      });
  }

  onReject(): void {
    if (!this.employee) return;
    this.isRejectionModalVisible = true;
  }

  
  closeRejectionModal(): void {
    this.isRejectionModalVisible = false;
  }


  onRejectionSubmit(reason: string): void {
    if (!this.employee) return;
    this.closeRejectionModal(); // Close the modal first
    this.isRejecting = true;

    this.employeeService.rejectEmployeeDocuments(this.employee.id, reason)
      .pipe(finalize(() => this.isRejecting = false))
      .subscribe({
        next: () => {
          this.notificationService.show('Employee\'s documents rejected successfully!', 'info');
          this.router.navigate(['/organization/employees']);
        },
        error: (err) => alert(`Rejection failed: ${err.error?.message || 'Server Error'}`)
      });
  }

  ngOnDestroy(): void {
    if (this.salarySub) {
      this.salarySub.unsubscribe();
    }
  }
}