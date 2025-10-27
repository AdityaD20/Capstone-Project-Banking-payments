import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { EligibleEmployee, PaymentService } from '../../../core/services/payment.service';
import { finalize, take } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';

@Component({
  selector: 'app-payroll',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DecimalPipe],
  templateUrl: './payroll.component.html',
  styleUrls: ['./payroll.component.css'], // Converted to CSS
})
export class PayrollComponent implements OnInit {
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isSubmitting = false;

  payrollForm: FormGroup;
  totalPayrollCost = 0;
  totalEmployeesSelected = 0;
  currentMonth: string;

  constructor(
    private paymentService: PaymentService,
    private fb: FormBuilder,
    private notificationService: NotificationService,
    private confirmationService: ConfirmationService
  ) {
    this.payrollForm = this.fb.group({
      employees: this.fb.array([]),
    });

    this.currentMonth = new Date().toLocaleString('default', { month: 'long', year: 'numeric' });
  }

  ngOnInit(): void {
    this.paymentService.getEligibleEmployees(0, 200).subscribe({
      next: (response) => {
        if (response && response.content) {
          this.populateEmployeeForm(response.content);
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage =
          'Could not load eligible employees. Please ensure you have active employees with bank and salary details configured.';
        this.isLoading = false;
      },
    });
  }

  get employees(): FormArray {
    return this.payrollForm.get('employees') as FormArray;
  }

  populateEmployeeForm(employees: EligibleEmployee[]): void {
    employees.forEach((emp) => {
      this.employees.push(
        this.fb.group({
          selected: [false],
          employeeId: [emp.employeeId],
          employeeNumber: [emp.employeeNumber],
          fullName: [emp.fullName],
          defaultNetSalary: [emp.defaultNetSalary],
        })
      );
    });

    this.payrollForm.valueChanges.subscribe((values) => {
      if (values.employees) {
        this.calculateTotals(values.employees);
      }
    });
  }

  calculateTotals(employees: any[]): void {
    this.totalPayrollCost = employees
      .filter((emp) => emp.selected)
      .reduce((sum, emp) => sum + (Number(emp.defaultNetSalary) || 0), 0);

    this.totalEmployeesSelected = employees.filter((emp) => emp.selected).length;
  }

  onSubmit(): void {
    if (this.totalEmployeesSelected === 0) {
      // REPLACED alert()
      this.notificationService.show('Please select at least one employee to pay.', 'warning');
      return;
    }

    // REPLACED confirm()
    this.confirmationService
      .confirm({
        title: 'Confirm Payroll Submission',
        message: `You are about to submit a payroll request for ${
          this.totalEmployeesSelected
        } employees, totaling ₹${this.totalPayrollCost.toFixed(2)}. Do you want to proceed?`,
        confirmText: 'Submit Request',
      })
      .pipe(take(1))
      .subscribe((result) => {
        if (result) {
          this.isSubmitting = true;
          this.errorMessage = null;
          this.successMessage = null;

          const selectedEmployees = this.payrollForm.value.employees
            .filter((emp: any) => emp.selected)
            .map((emp: any) => ({
              employeeId: emp.employeeId,
              amount: Number(emp.defaultNetSalary),
            }));

          this.paymentService
            .disburseSalaries({ employeesToPay: selectedEmployees })
            .pipe(finalize(() => (this.isSubmitting = false)))
            .subscribe({
              next: (response) => {
                this.successMessage = response;
                this.payrollForm.disable(); // Disable the form after successful submission
              },
              error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to submit payroll request.';
              },
            });
        }
      });
  }
}
