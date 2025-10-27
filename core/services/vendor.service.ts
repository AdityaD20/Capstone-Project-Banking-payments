import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, map, Observable, tap, throwError } from 'rxjs';

// Interface to match the VendorResponseDto
export interface Vendor {
  id: number;
  name: string;
  contactEmail: string;
  contactPhone: string;
  status: 'ACTIVE' | 'DISABLED';
  organizationId: number;
}

// Interface to match the VendorCreateRequestDto
export interface AddVendorPayload {
  name: string;
  email: string;
  phone: string;
  accountNumber: string;
  ifscCode: string;
  bankName: string;
}

@Injectable({
  providedIn: 'root'
})
export class VendorService {
  private apiUrl = 'https://localhost:8080/api/organizations/vendors';

  // 1. STATE MANAGEMENT
  private vendorsSubject = new BehaviorSubject<Vendor[]>([]);
  public vendors$ = this.vendorsSubject.asObservable();

  constructor(private http: HttpClient) { }

  // 2. DATA FETCHING METHOD
  fetchAndLoadVendors(): Observable<Vendor[]> {
    return this.http.get<{ content: Vendor[] }>(this.apiUrl, {
      params: { page: '0', size: '200', sort: 'name,asc' }
    }).pipe(
      map(response => response.content), // Extract the array from the 'content' property
      tap(vendors => this.vendorsSubject.next(vendors)),
      catchError(err => {
        this.vendorsSubject.next([]);
        return throwError(() => err);
      })
    );
  }

  // 3. ACTION METHOD
  addVendor(payload: AddVendorPayload): Observable<Vendor> {
    return this.http.post<Vendor>(this.apiUrl, payload).pipe(
      tap(newVendor => {
        const currentVendors = this.vendorsSubject.getValue();
        this.vendorsSubject.next([...currentVendors, newVendor]);
      })
    );
  }

}