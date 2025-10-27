import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';

// --- INTERFACES (unchanged) ---
export interface DepositRequestPayload {
  amount: number;
  description: string;
}
export interface DepositResponse {
    id: number;
    organizationName: string;
    amount: number;
    status: 'PENDING' | 'APPROVED' | 'REJECTED';
    description: string;
    rejectionReason: string | null;
    createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DepositService {
  private apiUrl = 'https://localhost:8080/api/organizations/deposits';

  // 1. STATE MANAGEMENT
  private historySubject = new BehaviorSubject<DepositResponse[]>([]);
  public history$ = this.historySubject.asObservable();

  constructor(private http: HttpClient) { }

  // 2. DATA FETCHING
  fetchAndLoadHistory(): Observable<DepositResponse[]> {
    return this.http.get<DepositResponse[]>(`${this.apiUrl}/history`).pipe(
      tap(history => this.historySubject.next(history)),
      catchError(err => {
        this.historySubject.next([]);
        return throwError(() => err);
      })
    );
  }

  // 3. ACTION METHOD
  requestDeposit(payload: DepositRequestPayload, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('deposit', JSON.stringify(payload));
    formData.append('file', file, file.name);
    
    // We can't easily update state here since the response is just a string.
    // So after the request is successful, we will trigger a re-fetch.
    return this.http.post(`${this.apiUrl}/request`, formData, { responseType: 'text' }).pipe(
      tap(() => {
        this.fetchAndLoadHistory().subscribe(); // Re-fetch the list
      })
    );
  }

}