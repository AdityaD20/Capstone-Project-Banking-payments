import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, DatePipe, TitleCasePipe } from '@angular/common';
import { Concern, ConcernService, Document, Page } from '../../../core/services/concern.service'; // Import Page if needed
import { Observable, catchError, of, map, finalize } from 'rxjs';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Modal } from 'bootstrap';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-concerns',
  standalone: true,
  imports: [CommonModule, DatePipe, TitleCasePipe, ReactiveFormsModule, FormsModule],
  templateUrl: './concerns.component.html',
  styleUrls: ['./concerns.component.css']
})
export class ConcernsComponent implements OnInit {
  // Now using the observable directly from the service
  concerns$: Observable<Concern[]>;
  isLoading = true;
  errorMessage: string | null = null;
  filterStatus: string = '';

  // Modal Properties
  selectedConcern: Concern | null = null;
  private detailModalInstance: Modal | null = null;
  @ViewChild('concernDetailModal') detailModalElement!: ElementRef;
  
  responseForm: FormGroup;
  isSubmittingResponse = false;
  responseError: string | null = null;

  attachments$!: Observable<Document[]>;
  isLoadingAttachments = false;

  constructor(
    private concernService: ConcernService, 
    private fb: FormBuilder,
    private notificationService: NotificationService
    ) {
    this.responseForm = this.fb.group({
      responseText: ['', Validators.required]
    });
    // Assign the observable from the service
    this.concerns$ = this.concernService.orgConcerns$;
  }

  ngOnInit(): void {
    this.loadConcerns();
  }

  loadConcerns(): void {
    this.isLoading = true;
    this.errorMessage = null;
    // **THE FIX IS HERE: Call the new reactive method**
    this.concernService.fetchAndLoadAllOrgConcerns(this.filterStatus || undefined).pipe(
        catchError((err: any) => { // Added type
            this.errorMessage = "Could not load concerns.";
            return of([]);
        })
    ).subscribe(() => {
        this.isLoading = false;
    });
  }

  onFilterChange(): void {
    this.loadConcerns();
  }

  openDetailModal(concern: Concern): void {
    this.selectedConcern = concern;
    this.responseForm.reset();
    this.responseError = null;

    this.isLoadingAttachments = true;
    this.attachments$ = this.concernService.getConcernAttachments(concern.id)
        .pipe(finalize(() => this.isLoadingAttachments = false));

    if (!this.detailModalInstance) {
      this.detailModalInstance = new Modal(this.detailModalElement.nativeElement);
    }
    this.detailModalInstance.show();
  }

  closeDetailModal(): void {
    if (this.detailModalInstance) {
      this.detailModalInstance.hide();
    }
    this.selectedConcern = null;
  }

  submitResponse(): void {
    if (this.responseForm.invalid || !this.selectedConcern) return;

    this.isSubmittingResponse = true;
    this.responseError = null;
    const payload = this.responseForm.value;

    this.concernService.addResponseToConcern(this.selectedConcern.id, payload)
      .pipe(finalize(() => this.isSubmittingResponse = false))
      .subscribe({
        next: () => {
          this.notificationService.show('Response submitted successfully.', 'success');
          this.responseForm.reset();
          // It's good practice to also refresh the comments/responses after adding one
          this.openDetailModal(this.selectedConcern!);
        },
        error: (err) => this.responseError = err.error?.message || "Failed to submit response."
      });
  }

  updateStatus(newStatus: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'): void {
    if (!this.selectedConcern) return;
    
    this.concernService.updateConcernStatus(this.selectedConcern.id, { newStatus })
      .subscribe({
        next: (updatedConcern) => {
          this.notificationService.show(`Status updated to ${newStatus}`, 'info');
          // The service's BehaviorSubject will automatically update the main list.
          // We just need to update the object for the currently open modal.
          if (this.selectedConcern) {
            this.selectedConcern.status = updatedConcern.status;
          }
        },
        error: () => this.notificationService.show('Failed to update status.', 'danger')
      });
  }
}