import { Component } from '@angular/core';
import { CommonModule, DatePipe, TitleCasePipe } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms'; // 1. Import AbstractControl and ValidationErrors
import { ReportService, TransactionReport } from '../../../core/services/report.service';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe, TitleCasePipe],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent {
  filterForm: FormGroup;
  
  reportData: TransactionReport[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  
  isDownloadingPdf = false;
  isDownloadingExcel = false;

  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;

  constructor(
    private fb: FormBuilder, 
    private reportService: ReportService,
    private notificationService: NotificationService
    ) {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split('T')[0];
    const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split('T')[0];

    this.filterForm = this.fb.group({
      from: [firstDay, Validators.required],
      to: [lastDay, Validators.required],
      type: ['']
    }, { 
      validators: this.dateRangeValidator // 2. APPLY THE CUSTOM VALIDATOR TO THE GROUP
    });
  }

  // 3. DEFINE THE CUSTOM VALIDATOR FUNCTION
  private dateRangeValidator(control: AbstractControl): ValidationErrors | null {
    const fromDate = control.get('from')?.value;
    const toDate = control.get('to')?.value;

    // Only validate if both fields have values
    if (fromDate && toDate && fromDate > toDate) {
      // Return an error object if validation fails
      return { dateRangeInvalid: true };
    }

    // Return null if validation passes
    return null;
  }

  generateReport(page: number = 0): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.currentPage = page;

    const { from, to, type } = this.filterForm.value;

    this.reportService.viewReport(from, to, type || null, this.currentPage, this.pageSize)
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (response) => {
          this.reportData = response.content;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
        },
        error: (err) => {
          this.errorMessage = err.error?.message || "Failed to generate report.";
        }
      });
  }

  download(format: 'pdf' | 'excel'): void {
    if (this.filterForm.invalid) {
      this.filterForm.markAllAsTouched();
      return;
    }

    if (format === 'pdf') this.isDownloadingPdf = true;
    if (format === 'excel') this.isDownloadingExcel = true;

    const { from, to, type } = this.filterForm.value;
    const serviceMethod = format === 'pdf' 
        ? this.reportService.downloadPdf(from, to, type || null)
        : this.reportService.downloadExcel(from, to, type || null);
    
    serviceMethod
      .pipe(finalize(() => {
        if (format === 'pdf') this.isDownloadingPdf = false;
        if (format === 'excel') this.isDownloadingExcel = false;
      }))
      .subscribe({
        next: (response) => {
          if (response.body) {
            const blob = new Blob([response.body]);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            
            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = `transaction-report.${format === 'pdf' ? 'pdf' : 'xlsx'}`;
            if (contentDisposition) {
              const filenameRegex = /filename[^;=\n]*=(?:"([^"]*)"|([^;\n]*))/;
              const matches = filenameRegex.exec(contentDisposition);
              if (matches != null && (matches[1] || matches[2])) {
                filename = matches[1] || matches[2];
              }
            }
            a.download = filename;
            a.click();
            window.URL.revokeObjectURL(url);
          }
        },
        error: (err) => this.notificationService.show('Download failed. Please try again.', 'danger')
      });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.generateReport(page);
    }
  }
}