import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, take } from 'rxjs';
import { Modal } from 'bootstrap';
import { AdminService, Document, FinalApprovalPayload, Organization } from '../../../core/services/admin.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';
import { RejectionDialogComponent } from '../../../components/rejection-dialog/rejection-dialog.component';
import { DocNamePipe } from '../../../pipes/doc-name.pipe';

@Component({
  selector: 'app-pending-approvals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RejectionDialogComponent, DocNamePipe],
  templateUrl: './pending-approvals.component.html',
})
export class PendingApprovalsComponent implements OnInit {
  pendingInitialOrgs$!: Observable<Organization[]>;
  pendingFinalOrgs$!: Observable<Organization[]>;
  isLoading = true;
  errorMessage: string | null = null;

  // State for Modals
  selectedOrg: Organization | null = null;
  orgDocuments: Document[] = [];
  isLoadingDocuments = false;
  approvalForm: FormGroup;
  isSubmitting = false;
  private reviewModalInstance: Modal | null = null;
  @ViewChild('reviewModal') reviewModalElement!: ElementRef;
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
    this.pendingInitialOrgs$ = this.adminService.pendingInitialOrgs$;
    this.pendingFinalOrgs$ = this.adminService.pendingFinalOrgs$;
    
    this.approvalForm = this.fb.group({
      accountNumber: ['', Validators.required],
      ifscCode: ['', Validators.required],
      bankName: ['', Validators.required],
      initialBalance: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.adminService.fetchAllPending().subscribe({
      next: () => this.isLoading = false,
      error: (err) => {
        this.errorMessage = "Failed to load pending approvals.";
        this.isLoading = false;
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

  rejectInitial(org: Organization): void {
    const action = (reason: string) => {
      this.adminService.rejectInitialRegistration(org.id, reason).subscribe({
        next: () => this.notificationService.show('Organization rejected.', 'info'),
        error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
      });
    };
    this.openRejectionModal(`Reject ${org.name}`, 'Provide a reason for initial rejection:', action);
  }
  
  submitFinalApproval(): void {
    if (this.approvalForm.invalid || !this.selectedOrg) return;
    this.isSubmitting = true;
    const payload: FinalApprovalPayload = this.approvalForm.value;
    this.adminService.approveFinalRegistration(this.selectedOrg.id, payload).subscribe({
        next: () => {
          this.notificationService.show('Organization activated!', 'success');
          this.closeReviewModal();
        },
        error: (err) => this.notificationService.show(`Activation failed: ${err.error?.message}`, 'danger')
      }).add(() => this.isSubmitting = false);
  }

  submitDocumentRejection(): void {
    if (!this.selectedOrg) return;
    const orgName = this.selectedOrg.name;
    const action = (reason: string) => {
      this.isSubmitting = true;
      this.adminService.rejectDocumentApproval(this.selectedOrg!.id, reason).subscribe({
          next: () => {
            this.notificationService.show('Documents rejected.', 'info');
            this.closeReviewModal();
          },
          error: (err) => this.notificationService.show(`Rejection Failed: ${err.error?.message}`, 'danger')
        }).add(() => this.isSubmitting = false);
    };
    if (this.reviewModalInstance) this.reviewModalInstance.hide();
    this.openRejectionModal(`Reject Documents for ${orgName}`, 'Provide a reason for rejecting the documents:', action);
  }

  // --- Modal Helpers ---
  openReviewModal(org: Organization): void {
    this.selectedOrg = org;
    this.isLoadingDocuments = true;
    this.orgDocuments = [];
    this.approvalForm.reset();
    if (!this.reviewModalInstance) { this.reviewModalInstance = new Modal(this.reviewModalElement.nativeElement); }
    this.reviewModalInstance.show();
    this.adminService.getOrganizationDocuments(org.id).subscribe(docs => {
      this.orgDocuments = docs;
      this.isLoadingDocuments = false;
    });
  }

  closeReviewModal(): void {
    if (this.reviewModalInstance) { this.reviewModalInstance.hide(); }
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