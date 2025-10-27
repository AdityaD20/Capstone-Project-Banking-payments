import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { PayslipService, PayslipSummary } from '../../core/services/payslip.service';
import { Observable, catchError, of, map } from 'rxjs';

@Component({
  selector: 'app-payslip-list',
  standalone: true,
  imports: [CommonModule, DatePipe, DecimalPipe],
  templateUrl: './payslip-list.component.html',
})
export class PayslipListComponent implements OnInit {
  payslips$!: Observable<PayslipSummary[]>;
  isLoading = true;
  errorMessage: string | null = null;

  // We use a string to track the action type ('view' or 'download') and the ID
  processingAction: { id: number, type: 'view' | 'download' } | null = null;
  actionError: string | null = null;

  constructor(private payslipService: PayslipService) {}

  ngOnInit(): void {
    this.payslips$ = this.payslipService.getMyPayslipHistory().pipe(
      map(response => response.content),
      catchError(err => {
        this.errorMessage = "Could not load payslip history. Please try again later.";
        return of([]);
      })
    );
  }

  // --- Method to VIEW the PDF in a new tab ---
  viewPdfInNewTab(payslip: PayslipSummary): void {
    this.processingAction = { id: payslip.id, type: 'view' };
    this.actionError = null;

    this.payslipService.downloadPayslipPdf(payslip.id).subscribe({
      next: (response) => {
        if (response.body) {
          const blob = new Blob([response.body], { type: 'application/pdf' });
          const url = window.URL.createObjectURL(blob);
          window.open(url, '_blank');
          setTimeout(() => window.URL.revokeObjectURL(url), 100);
        }
        this.processingAction = null;
      },
      error: (err) => {
        this.actionError = `Failed to open PDF for payslip #${payslip.id}.`;
        this.processingAction = null;
      }
    });
  }

  // --- Method to DOWNLOAD the PDF ---
  downloadPdf(payslip: PayslipSummary): void {
    this.processingAction = { id: payslip.id, type: 'download' };
    this.actionError = null;

    this.payslipService.downloadPayslipPdf(payslip.id).subscribe({
      next: (response) => {
        if (response.body) {
          const blob = new Blob([response.body], { type: 'application/pdf' });
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          
          const contentDisposition = response.headers.get('Content-Disposition');
          let filename = `PaySlip-${payslip.payPeriod}.pdf`;
          if (contentDisposition) {
              const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
              const matches = filenameRegex.exec(contentDisposition);
              if (matches != null && matches[1]) {
                  filename = matches[1].replace(/['"]/g, '');
              }
          }
          
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a);
        }
        this.processingAction = null;
      },
      error: (err) => {
        this.actionError = `Failed to download PDF for payslip #${payslip.id}.`;
        this.processingAction = null;
      }
    });
  }
}