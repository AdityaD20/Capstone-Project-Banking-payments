import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Observable, take } from 'rxjs';
import { Modal } from 'bootstrap';
import { AdminService, DepositRequest, Document } from '../../../core/services/admin.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RejectionDialogComponent } from '../../../components/rejection-dialog/rejection-dialog.component';

@Component({
  selector: 'app-deposit-requests',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, RejectionDialogComponent],
  templateUrl: './deposit-requests.component.html',
})
export class DepositRequestsComponent implements OnInit {
  pendingDepositRequests$!: Observable<DepositRequest[]>;
  isLoading = true;
  errorMessage: string | null = null;

  selectedDeposit: DepositRequest | null = null;
  depositDocument$!: Observable<Document[]>;
  isLoadingDepositDoc = false;
  private depositModalInstance: Modal | null = null;
  @ViewChild('depositReviewModal') depositModalElement!: ElementRef;
  
  isRejectionModalVisible = false;
  rejectionTitle = '';
  rejectionPrompt = '';
  private rejectionAction: ((reason: string) => void) | null = null;

  constructor(
    private adminService: AdminService,
    private confirmationService: ConfirmationService,
    private notificationService: NotificationService
  ) {
    this.pendingDepositRequests$ = this.adminService.pendingDeposits$;
  }

  ngOnInit(): void {
    this.adminService.fetchAllPending().subscribe({
      next: () => this.isLoading = false,
      error: (err) => {
        this.errorMessage = "Failed to load deposit requests.";
        this.isLoading = false;
      }
    });
  }

  approveDeposit(request: DepositRequest): void {
    this.confirmationService.confirm({
      title: 'Confirm Deposit',
      message: `Approve deposit of ₹${request.amount} for "${request.organizationName}"?`,
      confirmText: 'Approve Deposit'
    }).pipe(take(1)).subscribe(result => {
      if (result) {
        this.adminService.approveDepositRequest(request.id).subscribe({
            next: () => {
              this.notificationService.show('Deposit approved!', 'success');
              this.closeDepositModal();
            },
            error: (err) => this.notificationService.show(`Approval failed: ${err.error?.message}`, 'danger')
        });
      }
    });
  }
  
  rejectDeposit(request: DepositRequest): void {
    const action = (reason: string) => {
      this.adminService.rejectDepositRequest(request.id, reason).subscribe({
        next: () => {
          this.notificationService.show('Deposit rejected.', 'info');
          this.closeDepositModal();
        },
        error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
      });
    };
    if (this.depositModalInstance) this.depositModalInstance.hide();
    this.openRejectionModal(`Reject Deposit for ${request.organizationName}`, 'Provide a reason for this rejection:', action);
  }

  openDepositReviewModal(request: DepositRequest): void {
    this.selectedDeposit = request;
    this.isLoadingDepositDoc = true;
    this.depositDocument$ = this.adminService.getDepositRequestDocument(request.id);
    if (!this.depositModalInstance) { this.depositModalInstance = new Modal(this.depositModalElement.nativeElement); }
    this.depositModalInstance.show();
  }
  
  closeDepositModal(): void {
    if (this.depositModalInstance) { this.depositModalInstance.hide(); }
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