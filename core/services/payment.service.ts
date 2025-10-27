import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Interface for an employee eligible for payroll (from your DTO)
export interface EligibleEmployee {
  employeeId: number;
  employeeNumber: string;
  fullName: string;
  defaultNetSalary: number;
}

// Interface for the payload to disburse salaries
export interface SalaryDisbursementPayload {
  employeesToPay: {
    employeeId: number;
    amount: number;
  }[];
}

export interface VendorPaymentPayload {
  vendorId: number;
  amount: number;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private orgApiUrl = 'https://localhost:8080/api/organizations';
  private paymentApiUrl = 'https://localhost:8080/api/payments';

  constructor(private http: HttpClient) { }

  /**
   * Fetches all employees eligible for the current payroll run.
   */
  getEligibleEmployees(page: number, size: number): Observable<any> {
    return this.http.get<any>(`${this.orgApiUrl}/employees/eligible-for-payroll`, {
      params: {
        page: page.toString(),
        size: size.toString()
      }
    });
  }

  /**
   * Submits a salary disbursement request for admin approval.
   */
  disburseSalaries(payload: SalaryDisbursementPayload): Observable<string> {
    return this.http.post(`${this.paymentApiUrl}/salary/disburse`, payload, { responseType: 'text' });
  }

  createVendorPayment(payload: VendorPaymentPayload): Observable<string> {
    return this.http.post(`${this.paymentApiUrl}/vendor`, payload, { responseType: 'text' });
  }
}