import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, forkJoin, map, Observable, tap } from 'rxjs';

// Defines the data structure for an Organization object
export interface Organization {
  id: number;
  name: string;
  status: string;
  email: string;
  createdAt: string;
  balance: number; 
  rejectionReason?: string;
}

export interface Document {
  id: number;
  url: string;
  type: string;
  displayName: string;
}

export interface FinalApprovalPayload {
  accountNumber: string;
  ifscCode: string;
  bankName: string;
  initialBalance: number; 
}

export interface PaymentRequest {
  id: number;
  organizationId: number;
  organizationName: string;
  amount: number;
  status: string;
  type: 'SALARY' | 'VENDOR';
  description: string;
  rejectionReason: string | null;
  createdAt: string;
}

export interface DepositRequest {
  id: number;
  organizationName: string;
  amount: number;
  status: string;
  description: string;
  rejectionReason: string | null;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private orgApiUrl = 'https://localhost:8080/api/bank-admin/organizations';
  private paymentApiUrl = 'https://localhost:8080/api/bank-admin/organizations/payment-requests';
  private depositApiUrl = 'https://localhost:8080/api/bank-admin/organizations/deposits';

  // 1. STATE MANAGEMENT: Subjects for each list
  private pendingInitialOrgsSubject = new BehaviorSubject<Organization[]>([]);
  private pendingFinalOrgsSubject = new BehaviorSubject<Organization[]>([]);
  private pendingPaymentsSubject = new BehaviorSubject<PaymentRequest[]>([]);
  private pendingDepositsSubject = new BehaviorSubject<DepositRequest[]>([]);

  // 2. SELECTORS: Expose observables for components to use
  public pendingInitialOrgs$ = this.pendingInitialOrgsSubject.asObservable();
  public pendingFinalOrgs$ = this.pendingFinalOrgsSubject.asObservable();
  public pendingDeposits$ = this.pendingDepositsSubject.asObservable();
  
  // Derived selectors for payment types
  public pendingSalaryRequests$ = this.pendingPaymentsSubject.asObservable().pipe(
    map(reqs => reqs.filter(r => r.type === 'SALARY'))
  );
  public pendingVendorRequests$ = this.pendingPaymentsSubject.asObservable().pipe(
    map(reqs => reqs.filter(r => r.type === 'VENDOR'))
  );

  constructor(private http: HttpClient) {}

  // 3. DATA FETCHING: A single method to fetch all data and update all states
  fetchAllPending(): Observable<any> {
    return forkJoin({
      initialOrgs: this.getOrganizations(['PENDING_INITIAL_APPROVAL']),
      finalOrgs: this.getOrganizations(['PENDING_FINAL_APPROVAL']),
      payments: this.getPendingPaymentRequests(),
      deposits: this.getPendingDepositRequests()
    }).pipe(
      tap(results => {
        this.pendingInitialOrgsSubject.next(results.initialOrgs);
        this.pendingFinalOrgsSubject.next(results.finalOrgs);
        this.pendingPaymentsSubject.next(results.payments);
        this.pendingDepositsSubject.next(results.deposits);
      })
    );
  }

  // 4. ACTION METHODS: Now they update the state on success

  approveInitialRegistration(id: number): Observable<Organization> {
    return this.http.post<Organization>(`${this.orgApiUrl}/${id}/approve-initial`, {}).pipe(
      tap(() => {
        // Remove the organization from the initial pending list
        const updatedOrgs = this.pendingInitialOrgsSubject.getValue().filter(org => org.id !== id);
        this.pendingInitialOrgsSubject.next(updatedOrgs);
      })
    );
  }

  rejectInitialRegistration(id: number, reason: string): Observable<Organization> {
    const payload = { reason };
    return this.http.post<Organization>(`${this.orgApiUrl}/${id}/reject-initial`, payload).pipe(
      tap(() => {
        // Remove the organization from the initial pending list
        const updatedOrgs = this.pendingInitialOrgsSubject.getValue().filter(org => org.id !== id);
        this.pendingInitialOrgsSubject.next(updatedOrgs);
      })
    );
  }

  approveFinalRegistration(orgId: number, payload: FinalApprovalPayload): Observable<Organization> {
    return this.http.post<Organization>(`${this.orgApiUrl}/${orgId}/approve-final`, payload).pipe(
      tap(() => {
        // Remove from final pending list
        const updatedOrgs = this.pendingFinalOrgsSubject.getValue().filter(org => org.id !== orgId);
        this.pendingFinalOrgsSubject.next(updatedOrgs);
      })
    );
  }

  rejectDocumentApproval(orgId: number, reason: string): Observable<Organization> {
    const payload = { reason };
    return this.http.post<Organization>(`${this.orgApiUrl}/${orgId}/reject-documents`, payload).pipe(
      tap(() => {
        // Remove from final pending list
        const updatedOrgs = this.pendingFinalOrgsSubject.getValue().filter(org => org.id !== orgId);
        this.pendingFinalOrgsSubject.next(updatedOrgs);
      })
    );
  }

  approvePaymentRequest(requestId: number): Observable<PaymentRequest> {
    return this.http.post<PaymentRequest>(`${this.paymentApiUrl}/${requestId}/approve`, {}).pipe(
      tap(() => {
        // Remove from payments list
        const updatedPayments = this.pendingPaymentsSubject.getValue().filter(p => p.id !== requestId);
        this.pendingPaymentsSubject.next(updatedPayments);
      })
    );
  }

  rejectPaymentRequest(requestId: number, reason: string): Observable<PaymentRequest> {
    const payload = { reason };
    return this.http.post<PaymentRequest>(`${this.paymentApiUrl}/${requestId}/reject`, payload).pipe(
      tap(() => {
        // Remove from payments list
        const updatedPayments = this.pendingPaymentsSubject.getValue().filter(p => p.id !== requestId);
        this.pendingPaymentsSubject.next(updatedPayments);
      })
    );
  }
  
  approveDepositRequest(depositId: number): Observable<DepositRequest> {
    return this.http.post<DepositRequest>(`${this.depositApiUrl}/${depositId}/approve`, {}).pipe(
      tap(() => {
        // Remove from deposits list
        const updatedDeposits = this.pendingDepositsSubject.getValue().filter(d => d.id !== depositId);
        this.pendingDepositsSubject.next(updatedDeposits);
      })
    );
  }

  rejectDepositRequest(depositId: number, reason: string): Observable<DepositRequest> {
    const payload = { reason };
    return this.http.post<DepositRequest>(`${this.depositApiUrl}/${depositId}/reject`, payload).pipe(
      tap(() => {
        // Remove from deposits list
        const updatedDeposits = this.pendingDepositsSubject.getValue().filter(d => d.id !== depositId);
        this.pendingDepositsSubject.next(updatedDeposits);
      })
    );
  }
  
  // --- PRIVATE/UNCHANGED HELPERS ---
  // These are private methods used by the public fetchAllPending method
  public getOrganizations(statuses: string[]): Observable<Organization[]> {
    return this.http.get<Organization[]>(this.orgApiUrl, { params: { statuses } });
  }
  private getPendingPaymentRequests(): Observable<PaymentRequest[]> {
    return this.http.get<PaymentRequest[]>(`${this.orgApiUrl}/payment-requests/pending`);
  }
  private getPendingDepositRequests(): Observable<DepositRequest[]> {
    return this.http.get<DepositRequest[]>(`${this.depositApiUrl}/pending`);
  }

  // This method doesn't affect the dashboard's lists, so it remains unchanged
  getOrganizationDocuments(orgId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.orgApiUrl}/${orgId}/documents`);
  }
  getDepositRequestDocument(depositId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.depositApiUrl}/${depositId}/document`);
  }

  softDeleteOrganization(id: number): Observable<void> {
    return this.http.delete<void>(`${this.orgApiUrl}/${id}`);
  }
}
