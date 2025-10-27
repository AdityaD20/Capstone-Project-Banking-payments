import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Interface to match the TransactionReportDto from your backend
export interface TransactionReport {
  transactionId: number;
  type: 'SALARY' | 'VENDOR';
  status: 'PENDING' | 'PAID' | 'REJECTED' | 'FAILED';
  amount: number;
  description: string;
  recipientName: string;
  transactionDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'https://localhost:8080/api/organizations/reports';

  constructor(private http: HttpClient) { }

  /**
   * Fetches a paginated transaction report.
   */
  viewReport(from: string, to: string, type: string | null, page: number, size: number): Observable<any> {
    let params = new HttpParams()
      .set('from', from)
      .set('to', to)
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (type) {
      params = params.append('type', type);
    }

    return this.http.get<any>(`${this.apiUrl}/view`, { params });
  }

  /**
   * Downloads the transaction report as a PDF.
   */
  downloadPdf(from: string, to: string, type: string | null): Observable<HttpResponse<Blob>> {
    let params = new HttpParams()
      .set('from', from)
      .set('to', to);
    
    if (type) {
      params = params.append('type', type);
    }
    
    return this.http.get(`${this.apiUrl}/download-pdf`, { params, observe: 'response', responseType: 'blob' });
  }

  /**
   * Downloads the transaction report as an Excel file.
   */
  downloadExcel(from: string, to: string, type: string | null): Observable<HttpResponse<Blob>> {
    let params = new HttpParams()
      .set('from', from)
      .set('to', to);
    
    if (type) {
      params = params.append('type', type);
    }

    return this.http.get(`${this.apiUrl}/download-excel`, { params, observe: 'response', responseType: 'blob' });
  }
}