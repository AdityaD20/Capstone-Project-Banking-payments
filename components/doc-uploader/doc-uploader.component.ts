import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrganizationService } from '../../core/services/organization.service';
import { finalize } from 'rxjs';

// Helper interface for managing document slots
interface DocumentSlot {
  key: string;
  label: string;
  file: File | null;
}

@Component({
  selector: 'app-doc-uploader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './doc-uploader.component.html'
})
export class DocUploaderComponent {
  // Define the four required document slots
  documentSlots: DocumentSlot[] = [
    { key: 'registration_certificate', label: 'Registration Certificate', file: null },
    { key: 'tax_certificate', label: 'Tax Certificate', file: null },
    { key: 'bank_passbook', label: 'Bank Passbook / Statement', file: null },
    { key: 'initial_balance_cheque', label: 'Cheque for Initial Balance', file: null }
  ];

  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  @Output() uploadSuccess = new EventEmitter<void>();

  constructor(private orgService: OrganizationService) {}

  onFileSelected(event: any, slot: DocumentSlot): void {
    this.errorMessage = null; // Clear previous errors
    const file = event.target.files[0] ?? null;

    if (file) {
      const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg'];
      if (!allowedTypes.includes(file.type)) {
        this.errorMessage = `Invalid file type for "${slot.label}". Please upload a PDF or JPG file.`;
        slot.file = null; // Clear the invalid selection
        event.target.value = ''; // Reset the file input
        return;
      }
      slot.file = file;
    }
  }

  // Check if all required files have been selected
  get allFilesSelected(): boolean {
    return this.documentSlots.every(slot => slot.file !== null);
  }

  onUpload(): void {
    if (!this.allFilesSelected) {
      this.errorMessage = "Please select a file for all required documents.";
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    // Create a new array of Files with prefixed names
    const filesToUpload: File[] = this.documentSlots.map(slot => {
      // We create a new File object from the original blob but with a new name
      return new File([slot.file!], `${slot.key}_${slot.file!.name}`, { type: slot.file!.type });
    });

    this.orgService.uploadDocuments(filesToUpload)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.successMessage = 'All documents uploaded successfully! They are now pending final approval.';
          // You could optionally disable the form here
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'File upload failed. Please try again.';
        }
      });
  }
}