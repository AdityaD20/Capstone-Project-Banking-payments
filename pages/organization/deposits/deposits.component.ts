import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DepositRequestPayload, DepositResponse, DepositService } from '../../../core/services/deposit.service';
import { finalize, Observable, catchError, of } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-deposits',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CurrencyPipe, DatePipe, TitleCasePipe],
  templateUrl: './deposits.component.html',
  styleUrls: ['./deposits.component.css'] // Converted to CSS
})
export class DepositsComponent implements OnInit {
  depositForm: FormGroup;
  selectedFile: File | null = null;
  
  isSubmitting = false;
  submitError: string | null = null;

  // State for history list
  history$: Observable<DepositResponse[]>; // Now an observable
  isLoadingHistory = true;
  historyError: string | null = null;

  constructor(
    private fb: FormBuilder, 
    private depositService: DepositService,
    private notificationService: NotificationService
    ) {
    this.depositForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(1)]],
      description: ['', Validators.required]
    });
    // 1. Assign observable from service
    this.history$ = this.depositService.history$;
  }

  ngOnInit(): void {
    // 2. Trigger initial fetch
    this.loadHistory();
  }

  loadHistory(): void {
    this.isLoadingHistory = true;
    this.historyError = null;
    this.depositService.fetchAndLoadHistory().pipe(
      catchError(err => {
        this.historyError = "Could not load deposit history.";
        return of(null);
      })
    ).subscribe(() => {
      this.isLoadingHistory = false;
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0] ?? null;
  }

  onSubmit(): void {
    if (this.depositForm.invalid || !this.selectedFile) {
      this.depositForm.markAllAsTouched();
      if (!this.selectedFile) {
        this.submitError = "A proof of deposit document is required.";
      }
      return;
    }
    this.isSubmitting = true;
    this.submitError = null;
    
    const payload: DepositRequestPayload = this.depositForm.value;

    this.depositService.requestDeposit(payload, this.selectedFile)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response) => {
          this.notificationService.show(response, 'success');
          this.depositForm.reset();
          this.selectedFile = null;
          const fileInput = document.getElementById('depositFile') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
          // 3. NO MANUAL REFRESH. The service does it now.
        },
        error: (err) => {
          this.submitError = err.error?.message || 'Failed to submit deposit request.';
        }
      });
  }
}