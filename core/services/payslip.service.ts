import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Interface to match the PaySlipSummaryDto from your backend
export interface PayslipSummary {
  id: number;
  payPeriod: string; 
  netSalary: number;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class PayslipService {
  private apiUrl = 'https://localhost:8080/api/employees/me/payslips';

  constructor(private http: HttpClient) { }

  getMyPayslipHistory(): Observable<{ content: PayslipSummary[] }> {
    // We fetch a large page size to simplify the UI for now.
    return this.http.get<{ content: PayslipSummary[] }>(this.apiUrl, {
      params: { page: '0', size: '100', sort: 'payPeriod,desc' }
    });
  }

  
  downloadPayslipPdf(payslipId: number): Observable<HttpResponse<Blob>> {
    return this.http.get(`${this.apiUrl}/${payslipId}/download-pdf`, {
      observe: 'response', // Crucial to get the full HTTP response, including headers
      responseType: 'blob'  // Expect binary data (the PDF file)
    });
  }
}