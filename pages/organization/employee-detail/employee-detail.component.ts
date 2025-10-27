import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Employee, EmployeeService, UpdateBankAccountPayload, UpdateSalaryPayload } from '../../../core/services/employee.service';
import { finalize, Subscription } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service'; // 1. Import NotificationService

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, CurrencyPipe],
  templateUrl: './employee-detail.component.html',
  styleUrl: './employee-detail.component.css'
})
export class EmployeeDetailComponent implements OnInit, OnDestroy {
  employee: Employee | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  private salarySub!: Subscription;

  bankForm: FormGroup;
  isSubmittingBank = false;
  
  salaryForm: FormGroup;
  isSubmittingSalary = false;

  constructor(
    private route: ActivatedRoute,
    private employeeService: EmployeeService,
    private fb: FormBuilder,
    private router: Router,
    private notificationService: NotificationService // 2. Inject the service
  ) {
    this.bankForm = this.fb.group({
      accountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      bankName: ['', Validators.required],
    });

    this.salaryForm = this.fb.group({
      basicSalary: [0, [Validators.required, Validators.min(0)]],
      hra: [{ value: 0 }],
      da: [{ value: 0}],
      pf: [{ value: 0}],
      otherAllowances:[{ value: 0 }],
    });
  }

  ngOnInit(): void {
    this.salarySub = this.salaryForm.get('basicSalary')!.valueChanges.subscribe(basic => {
      if (typeof basic === 'number') {
        const hra = parseFloat((basic * 0.40).toFixed(2));
        const da = parseFloat((basic * 0.15).toFixed(2));
        const pf = parseFloat((basic * 0.12).toFixed(2));
        const otherAllowances = parseFloat((basic * 0.10).toFixed(2));


        this.salaryForm.patchValue({ hra, da, pf, otherAllowances }, { emitEvent: false });
      }
    });

    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.employeeService.getEmployeeById(id).subscribe({
        next: (emp) => {
          this.employee = emp;
          this.bankForm.patchValue({
            accountNumber: emp.bankAccount?.accountNumber,
            ifscCode: emp.bankAccount?.ifscCode,
            bankName: emp.bankAccount?.bankName
          });
          this.salaryForm.patchValue(emp.salaryStructure || {});
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = "Could not load employee details.";
          this.isLoading = false;
        }
      });
    }
  }

  onBankUpdate(): void {
    if (this.bankForm.invalid || !this.employee) return;
    this.isSubmittingBank = true;

    const payload: UpdateBankAccountPayload = this.bankForm.value;
    this.employeeService.updateBankAccount(this.employee.id, payload)
      .pipe(finalize(() => this.isSubmittingBank = false))
      .subscribe({
        next: () => {
          // 3. Replace alert() with the notification service
          this.notificationService.show('Bank account updated successfully!', 'success');
        },
        error: (err) => {
          // 3. Replace alert() with the notification service
          this.notificationService.show(`Update failed: ${err.error?.message}`, 'danger');
        }
      });
  }

  onSalaryUpdate(): void {
    if (this.salaryForm.invalid || !this.employee) return;
    this.isSubmittingSalary = true;

    const payload: UpdateSalaryPayload = this.salaryForm.value;
    this.employeeService.updateSalaryStructure(this.employee.id, payload)
      .pipe(finalize(() => this.isSubmittingSalary = false))
      .subscribe({
        next: () => {
          // 3. Replace alert() with the notification service
          this.notificationService.show('Salary structure updated successfully!', 'success');
        },
        error: (err) => {
          // 3. Replace alert() with the notification service
          this.notificationService.show(`Update failed: ${err.error?.message}`, 'danger');
        }
      });
  }

  ngOnDestroy(): void {
    if (this.salarySub) {
      this.salarySub.unsubscribe();
    }
  }
}