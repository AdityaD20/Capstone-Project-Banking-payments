import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Observable, take } from 'rxjs';
import { AdminService, PaymentRequest } from '../../../core/services/admin.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RejectionDialogComponent } from '../../../components/rejection-dialog/rejection-dialog.component';

@Component({
  selector: 'app-payment-requests',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RejectionDialogComponent],
  templateUrl: './payment-requests.component.html',
})
export class PaymentRequestsComponent implements OnInit {
  pendingSalaryRequests$!: Observable<PaymentRequest[]>;
  pendingVendorRequests$!: Observable<PaymentRequest[]>;
  isLoading = true;
  errorMessage: string | null = null;

  isRejectionModalVisible = false;
  rejectionTitle = '';
  rejectionPrompt = '';
  private rejectionAction: ((reason: string) => void) | null = null;

  constructor(
    private adminService: AdminService,
    private confirmationService: ConfirmationService,
    private notificationService: NotificationService
  ) {
    this.pendingSalaryRequests$ = this.adminService.pendingSalaryRequests$;
    this.pendingVendorRequests$ = this.adminService.pendingVendorRequests$;
  }

  ngOnInit(): void {
    this.adminService.fetchAllPending().subscribe({
      next: () => this.isLoading = false,
      error: (err) => {
        this.errorMessage = "Failed to load payment requests.";
        this.isLoading = false;
      }
    });
  }

  approvePayment(request: PaymentRequest): void {
    this.confirmationService.confirm({
      title: 'Confirm Approval',
      message: `Approve this ${request.type.toLowerCase()} payment for "${request.organizationName}"?`,
      confirmText: 'Approve'
    }).pipe(take(1)).subscribe(result => {
      if (result) {
        this.adminService.approvePaymentRequest(request.id).subscribe({
          next: () => this.notificationService.show('Payment approved successfully!', 'success'),
          error: (err) => this.notificationService.show(`Approval failed: ${err.error?.message || 'Server error'}`, 'danger')
        });
      }
    });
  }

  rejectPayment(request: PaymentRequest): void {
    const action = (reason: string) => {
      this.adminService.rejectPaymentRequest(request.id, reason).subscribe({
        next: () => this.notificationService.show('Payment request rejected.', 'info'),
        error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
      });
    };
    this.openRejectionModal(`Reject Payment for ${request.organizationName}`, 'Provide a reason for this payment rejection:', action);
  }
  
  openRejectionModal(title: string, prompt: string, action: (reason: string) => void): void {
    this.rejectionTitle = title;
    this.rejectionPrompt = prompt;
    this.rejectionAction = action;
    this.isRejectionModalVisible = true;
  }

  closeRejectionModal(): void {
    this.isRejectionModalVisible = false;
    this.rejectionAction = null;
  }

  onRejectionSubmit(reason: string): void {
    if (this.rejectionAction) {
      this.rejectionAction(reason);
    }
    this.closeRejectionModal();
  }
}