import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule, DatePipe, TitleCasePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Concern, ConcernService } from '../../../core/services/concern.service';
import { Observable, catchError, of, finalize } from 'rxjs';
import { Modal } from 'bootstrap';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-my-concerns',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe, TitleCasePipe],
  templateUrl: './my-concerns.component.html',
  styleUrls: ['./my-concerns.css']
})
export class MyConcernsComponent implements OnInit {
  concernForm: FormGroup;
  selectedFile: File | null = null;
  isSubmitting = false;
  submitError: string | null = null;

  // State for the history list
  concerns$: Observable<Concern[]>; // Now an observable
  isLoadingHistory = true;
  historyError: string | null = null;

  // State for the modal
  selectedConcern: Concern | null = null;
  private detailModalInstance: Modal | null = null;
  @ViewChild('concernDetailModal') detailModalElement!: ElementRef;
  comments$!: Observable<Concern[]>;
  isLoadingComments = false;

  constructor(
    private fb: FormBuilder, 
    private concernService: ConcernService,
    private notificationService: NotificationService
    ) {
    this.concernForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(10)]]
    });
    // 1. Assign the observable from the service
    this.concerns$ = this.concernService.myConcerns$;
  }

  ngOnInit(): void {
    // 2. Trigger the initial data fetch
    this.loadConcernHistory();
  }

  loadConcernHistory(): void {
    this.isLoadingHistory = true;
    this.historyError = null;
    this.concernService.fetchAndLoadMyConcerns().pipe(
      catchError(err => {
        this.historyError = "Could not load your concern history.";
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
    if (this.concernForm.invalid) {
      this.concernForm.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.submitError = null;
    const { description } = this.concernForm.value;
    
    this.concernService.createConcern(description, this.selectedFile)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: () => {
          this.notificationService.show("Your concern has been submitted successfully.", "success");
          this.concernForm.reset();
          this.selectedFile = null;
          const fileInput = document.getElementById('attachmentFile') as HTMLInputElement;
          if (fileInput) fileInput.value = '';
          // 3. NO MANUAL REFRESH. The service updated the state.
        },
        error: (err) => {
          this.submitError = err.error?.message || "An error occurred.";
        }
      });
  }

  openDetailModal(concern: Concern): void {
    this.selectedConcern = concern;
    this.isLoadingComments = true;
    this.comments$ = this.concernService.getMyConcernResponses(concern.id)
      .pipe(finalize(() => this.isLoadingComments = false));

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
}