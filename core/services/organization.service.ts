import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { Organization } from './admin.service';

export interface PaymentHistory {
  id: number;
  amount: number;
  status: 'PENDING' | 'PAID' | 'REJECTED' | 'FAILED';
  description: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private apiUrl = 'https://localhost:8080/api/organizations';

  // 1. STATE MANAGEMENT
  private organizationSubject = new BehaviorSubject<Organization | null>(null);
  public organization$ = this.organizationSubject.asObservable();

  private paymentHistorySubject = new BehaviorSubject<PaymentHistory[]>([]);
  public paymentHistory$ = this.paymentHistorySubject.asObservable();

  constructor(private http: HttpClient) {}

  // 2. DATA FETCHING METHODS
  fetchAndLoadMe(): Observable<Organization> {
    return this.http.get<Organization>(`${this.apiUrl}/me`).pipe(
      tap(org => this.organizationSubject.next(org)),
      catchError(err => {
        this.organizationSubject.next(null);
        return throwError(() => err);
      })
    );
  }

  fetchAndLoadPaymentHistory(): Observable<PaymentHistory[]> {
    return this.http.get<PaymentHistory[]>(`${this.apiUrl}/payment-history`).pipe(
      tap(history => this.paymentHistorySubject.next(history)),
      catchError(err => {
        this.paymentHistorySubject.next([]);
        return throwError(() => err);
      })
    );
  }

  // 3. ACTION METHOD (updates state on success)
  uploadDocuments(files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file, file.name);
    });
    return this.http.post(`${this.apiUrl}/documents/upload`, formData).pipe(
      tap(() => {
        // After uploading, re-fetch the organization's details to get the new status
        this.fetchAndLoadMe().subscribe();
      })
    );
  }
  
}