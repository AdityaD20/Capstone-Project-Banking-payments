import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EmployeeService } from '../../core/services/employee.service';
import { finalize } from 'rxjs';

// Helper interface for managing the document slots
interface DocumentSlot {
  key: string;
  label: string;
  file: File | null;
}

@Component({
  selector: 'app-employee-doc-uploader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './employee-doc-uploader.component.html',
})
export class EmployeeDocUploaderComponent {
  // Define the four required document slots for employees
  documentSlots: DocumentSlot[] = [
    { key: 'aadhar_card', label: 'Aadhar Card', file: null },
    { key: 'pan_card', label: 'PAN Card', file: null },
    { key: 'bank_passbook', label: 'Bank Passbook / Statement', file: null },
    { key: 'cancelled_cheque', label: 'Cancelled Cheque', file: null }
  ];

  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  @Output() uploadSuccess = new EventEmitter<void>();

  constructor(private employeeService: EmployeeService) {}

  onFileSelected(event: any, slot: DocumentSlot): void {
    this.errorMessage = null;
    const file = event.target.files[0] ?? null;

    if (file) {
      const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg'];
      if (!allowedTypes.includes(file.type)) {
        this.errorMessage = `Invalid file type for "${slot.label}". Please upload a PDF or JPG file.`;
        slot.file = null;
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
      // Create a new File object from the original file but with a new, prefixed name
      return new File([slot.file!], `${slot.key}_${slot.file!.name}`, { type: slot.file!.type });
    });

    this.employeeService.uploadMyDocuments(filesToUpload)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: () => {
          this.successMessage = 'All documents uploaded successfully! They are now pending review by your organization.';
          this.uploadSuccess.emit(); // Notify parent to refresh
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'File upload failed. Please try again.';
        }
      });
  }
}