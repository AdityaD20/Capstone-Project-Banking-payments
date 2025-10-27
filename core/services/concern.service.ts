import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, map, Observable, tap, throwError } from 'rxjs';

// Interface for a Concern object
export interface Concern {
  id: number;
  description: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  createdAt: string;
  employeeName: string; // For org view, holds employee name. For comment view, holds author email.
}

// Interface for the Page object from backend
export interface Page<T> {
  content: T[];
}

// Interface for a Document object
export interface Document {
  id: number;
  url: string;
  type: string;
}

// Interfaces for Payloads
export interface ConcernStatusUpdatePayload {
  newStatus: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
}
export interface ConcernCommentPayload {
  responseText: string;
}

@Injectable({ providedIn: 'root' })
export class ConcernService {
  private employeeApiUrl = 'https://localhost:8080/api/employees/me/concerns';
  private orgApiUrl = 'https://localhost:8080/api/organizations/concerns';

  // 1. STATE MANAGEMENT FOR THE ORGANIZATION VIEW
  private orgConcernsSubject = new BehaviorSubject<Concern[]>([]);
  public orgConcerns$ = this.orgConcernsSubject.asObservable();

  // 2. STATE MANAGEMENT FOR THE EMPLOYEE VIEW
  private myConcernsSubject = new BehaviorSubject<Concern[]>([]);
  public myConcerns$ = this.myConcernsSubject.asObservable();

  constructor(private http: HttpClient) {}

  // --- Methods for Employee (NOW REACTIVE) ---

  fetchAndLoadMyConcerns(): Observable<Concern[]> {
    return this.http
      .get<Page<Concern>>(this.employeeApiUrl, {
        params: { page: '0', size: '100', sort: 'createdAt,desc' },
      })
      .pipe(
        map((response) => response.content),
        tap((concerns) => this.myConcernsSubject.next(concerns)),
        catchError((err) => {
          this.myConcernsSubject.next([]);
          return throwError(() => err);
        })
      );
  }

  createConcern(description: string, attachment: File | null): Observable<Concern> {
    const formData = new FormData();
    formData.append('description', description);
    if (attachment) formData.append('attachment', attachment, attachment.name);

    return this.http.post<Concern>(this.employeeApiUrl, formData).pipe(
      tap((newConcern) => {
        const currentConcerns = this.myConcernsSubject.getValue();
        this.myConcernsSubject.next([newConcern, ...currentConcerns]); // Add to the top of the list
      })
    );
  }

  getMyConcernResponses(concernId: number): Observable<Concern[]> {
    return this.http.get<Concern[]>(`${this.employeeApiUrl}/${concernId}/responses`);
  }

  // --- Methods for Organization (NOW REACTIVE) ---
  fetchAndLoadAllOrgConcerns(status?: string): Observable<Concern[]> {
    let params: { [key: string]: string } = { page: '0', size: '100', sort: 'createdAt,desc' };
    if (status) params['status'] = status;

    return this.http.get<Page<Concern>>(this.orgApiUrl, { params }).pipe(
      map((response) => response.content),
      tap((concerns) => this.orgConcernsSubject.next(concerns)),
      catchError((err) => {
        this.orgConcernsSubject.next([]);
        return throwError(() => err);
      })
    );
  }

  updateConcernStatus(concernId: number, payload: ConcernStatusUpdatePayload): Observable<Concern> {
    return this.http.put<Concern>(`${this.orgApiUrl}/${concernId}/status`, payload).pipe(
      tap((updatedConcern) => {
        const currentConcerns = this.orgConcernsSubject.getValue();
        const index = currentConcerns.findIndex((c) => c.id === updatedConcern.id);
        if (index > -1) {
          const newConcerns = [...currentConcerns];
          newConcerns[index] = updatedConcern;
          this.orgConcernsSubject.next(newConcerns);
        }
      })
    );
  }

  // These methods don't modify state, so they remain the same
  addResponseToConcern(concernId: number, payload: ConcernCommentPayload): Observable<any> {
    return this.http.post(`${this.orgApiUrl}/${concernId}/respond`, payload, {
      responseType: 'text',
    });
  }
  getConcernAttachments(concernId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.orgApiUrl}/${concernId}/attachments`);
  }
}
