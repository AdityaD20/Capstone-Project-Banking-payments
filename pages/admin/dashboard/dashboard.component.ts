import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { AdminService, DepositRequest, Document, FinalApprovalPayload, Organization, PaymentRequest } from '../../../core/services/admin.service';
import { Observable, catchError, of, finalize, map, take, tap } from 'rxjs';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import { RejectionDialogComponent } from '../../../components/rejection-dialog/rejection-dialog.component';
import { DocNamePipe } from '../../../pipes/doc-name.pipe';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RejectionDialogComponent, DocNamePipe, CurrencyPipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  // Observables for tables AND summary counts
  pendingInitialOrgs$!: Observable<Organization[]>;
  pendingFinalOrgs$!: Observable<Organization[]>;
  pendingSalaryRequests$!: Observable<PaymentRequest[]>;
  pendingVendorRequests$!: Observable<PaymentRequest[]>;
  pendingDepositRequests$!: Observable<DepositRequest[]>;

  initialApprovalCount = 0;
  finalApprovalCount = 0;
  payrollRequestCount = 0;
  vendorRequestCount = 0;
  depositRequestCount = 0; // Added for consistency
  
  errorMessage: string | null = null;
  isLoading = true;

  // --- State for Modals (unchanged) ---
  selectedOrg: Organization | null = null;
  orgDocuments: Document[] = [];
  isLoadingDocuments = false;
  approvalForm: FormGroup;
  isSubmitting = false;
  private reviewModalInstance: Modal | null = null;
  @ViewChild('reviewModal') reviewModalElement!: ElementRef;
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
    private fb: FormBuilder,
    private confirmationService: ConfirmationService,
    private notificationService: NotificationService
  ) {
    this.approvalForm = this.fb.group({
      accountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      bankName: ['', Validators.required],
      initialBalance: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    // Assign observables from the reactive service
    this.pendingInitialOrgs$ = this.adminService.pendingInitialOrgs$.pipe(tap(orgs => this.initialApprovalCount = orgs.length));
    this.pendingFinalOrgs$ = this.adminService.pendingFinalOrgs$.pipe(tap(orgs => this.finalApprovalCount = orgs.length));
    this.pendingSalaryRequests$ = this.adminService.pendingSalaryRequests$.pipe(tap(reqs => this.payrollRequestCount = reqs.length));
    this.pendingVendorRequests$ = this.adminService.pendingVendorRequests$.pipe(tap(reqs => this.vendorRequestCount = reqs.length));
    this.pendingDepositRequests$ = this.adminService.pendingDeposits$.pipe(tap(reqs => this.depositRequestCount = reqs.length));
    
    // Trigger the initial fetch
    this.loadAllData();
  }

  loadAllData(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.adminService.fetchAllPending().pipe(
      catchError(err => {
        this.errorMessage = "Failed to load dashboard data. Please try again.";
        return of(null);
      })
    ).subscribe(() => {
      this.isLoading = false;
    });
  }

  // --- ACTION METHODS (These are identical to the logic we split into the new components) ---

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
  
  approveInitial(org: Organization): void {
    this.confirmationService.confirm({
      title: 'Confirm Approval',
      message: `Approve initial registration for "${org.name}"?`,
      confirmText: 'Approve'
    }).pipe(take(1)).subscribe(result => {
      if (result) {
        this.adminService.approveInitialRegistration(org.id).subscribe({
          next: () => this.notificationService.show('Organization approved!', 'success'),
          error: () => this.notificationService.show('Failed to approve organization.', 'danger')
        });
      }
    });
  }

  approveDeposit(request: DepositRequest): void {
    this.confirmationService.confirm({
      title: 'Confirm Deposit',
      message: `Approve deposit of $${request.amount} for "${request.organizationName}"?`,
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
  
  rejectInitial(org: Organization): void {
    const action = (reason: string) => {
      this.adminService.rejectInitialRegistration(org.id, reason).subscribe({
        next: () => this.notificationService.show('Organization rejected.', 'info'),
        error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
      });
    };
    this.openRejectionModal(`Reject ${org.name}`, 'Provide a reason for initial rejection:', action);
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

  submitFinalApproval(): void {
    if (this.approvalForm.invalid || !this.selectedOrg) return;
    this.isSubmitting = true;
    const payload: FinalApprovalPayload = this.approvalForm.value;
    this.adminService.approveFinalRegistration(this.selectedOrg.id, payload)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: () => {
          this.notificationService.show('Organization activated!', 'success');
          this.closeReviewModal();
        },
        error: (err) => this.notificationService.show(`Activation failed: ${err.error?.message}`, 'danger')
      });
  }

  submitDocumentRejection(): void {
    if (!this.selectedOrg) return;
    const orgName = this.selectedOrg.name;
    const action = (reason: string) => {
      this.isSubmitting = true;
      this.adminService.rejectDocumentApproval(this.selectedOrg!.id, reason)
        .pipe(finalize(() => this.isSubmitting = false))
        .subscribe({
          next: () => {
            this.notificationService.show('Documents rejected.', 'info');
            this.closeReviewModal();
          },
          error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
        });
    };
    if (this.reviewModalInstance) this.reviewModalInstance.hide();
    this.openRejectionModal(`Reject Documents for ${orgName}`, 'Provide a reason for rejecting the documents:', action);
  }

  // --- Modal helpers ---
  openReviewModal(org: Organization): void {
    this.selectedOrg = org;
    this.isLoadingDocuments = true;
    this.orgDocuments = [];
    this.approvalForm.reset();
    if (!this.reviewModalInstance) { this.reviewModalInstance = new Modal(this.reviewModalElement.nativeElement); }
    this.reviewModalInstance.show();
    this.adminService.getOrganizationDocuments(org.id).pipe(finalize(() => this.isLoadingDocuments = false)).subscribe(docs => this.orgDocuments = docs);
  }
  
  closeReviewModal(): void {
    if (this.reviewModalInstance) { this.reviewModalInstance.hide(); }
  }
  
  openDepositReviewModal(request: DepositRequest): void {
    this.selectedDeposit = request;
    this.isLoadingDepositDoc = true;
    this.depositDocument$ = this.adminService.getDepositRequestDocument(request.id).pipe(finalize(() => this.isLoadingDepositDoc = false));
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