import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap, map, catchError, throwError } from 'rxjs';

// --- INTERFACES (unchanged) ---
export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  employeeNumber?: string;
  status: 'PENDING_DOCUMENTS' | 'PENDING_APPROVAL' | 'ACTIVE' | 'DISABLED';
  rejectionReason?: string;
  bankAccount?: { accountNumber: string; bankName: string; ifscCode?: string };
  salaryStructure?: {
    basicSalary: number;
    hra: number;
    da: number;
    pf: number;
    otherAllowances: number;
  };
}

export interface AddEmployeePayload {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
}

export interface ApiDocument {
  id: number;
  url: string;
  type: string;
  displayName: string;
}

export interface EmployeeFinalizePayload {
  employeeNumber: string;
  accountNumber: string;
  ifscCode: string;
  bankName: string;
  basicSalary: number;
  hra: number;
  da: number;
  pf: number;
  otherAllowances: number;
}

export interface UpdateBankAccountPayload {
  accountNumber: string;
  ifscCode: string;
  bankName: string;
}

export interface UpdateSalaryPayload {
  basicSalary: number;
  hra: number;
  da: number;
  pf: number;
  otherAllowances: number;
}

@Injectable({
  providedIn: 'root',
})
export class EmployeeService {
  private orgApiUrl = 'https://localhost:8080/api/organizations/employees';
  private empApiUrl = 'https://localhost:8080/api/employees';

  // 1. STATE MANAGEMENT: The single source of truth for the employee list.
  private employeesSubject = new BehaviorSubject<Employee[]>([]);
  public employees$ = this.employeesSubject.asObservable();

  // 2. SELECTORS: Create derived observables for components to use.
  public pendingApprovalEmployees$ = this.employees$.pipe(
    map((employees) => employees.filter((emp) => emp.status === 'PENDING_APPROVAL'))
  );
  public allOtherEmployees$ = this.employees$.pipe(
    map((employees) => employees.filter((emp) => emp.status !== 'PENDING_APPROVAL'))
  );

  constructor(private http: HttpClient) {}

  // 3. DATA FETCHING: A single method to fetch data from the API and update the state.
  fetchAndLoadEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.orgApiUrl).pipe(
      tap((employees) => this.employeesSubject.next(employees)),
      catchError((err) => {
        // On error, clear the subject and propagate the error
        this.employeesSubject.next([]);
        return throwError(() => err);
      })
    );
  }

  getMe(): Observable<Employee> {
    return this.http.get<Employee>(`${this.empApiUrl}/me`);
  }

  uploadMyDocuments(files: File[]): Observable<Employee> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file, file.name);
    });
    return this.http.post<Employee>(`${this.empApiUrl}/me/documents/upload`, formData);
  }

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.orgApiUrl);
  }

  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.orgApiUrl}/${id}`);
  }

  addEmployee(payload: AddEmployeePayload): Observable<Employee> {
    return this.http.post<Employee>(this.orgApiUrl, payload).pipe(
      tap((newEmployee) => {
        const currentEmployees = this.employeesSubject.getValue();
        this.employeesSubject.next([...currentEmployees, newEmployee]);
      })
    );
  }

  batchUploadEmployees(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post(`${this.orgApiUrl}/batch-upload`, formData, { responseType: 'text' });
  }

  getEmployeeDocuments(employeeId: number): Observable<ApiDocument[]> {
    return this.http.get<ApiDocument[]>(`${this.orgApiUrl}/${employeeId}/documents`);
  }

  activateEmployee(employeeId: number, payload: EmployeeFinalizePayload): Observable<Employee> {
    return this.http.post<Employee>(`${this.orgApiUrl}/${employeeId}/activate`, payload).pipe(
      tap((updatedEmployee) => {
        this.updateEmployeeInState(updatedEmployee);
      })
    );
  }

  rejectEmployeeDocuments(employeeId: number, reason: string): Observable<Employee> {
    const payload = { reason };
    return this.http
      .post<Employee>(`${this.orgApiUrl}/${employeeId}/reject-documents`, payload)
      .pipe(
        tap((updatedEmployee) => {
          this.updateEmployeeInState(updatedEmployee);
        })
      );
  }
  softDeleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.orgApiUrl}/${id}`).pipe(
      tap(() => {
        // For soft delete, we can either refetch or update the status locally.
        // For simplicity and consistency, let's just refetch the list.
        this.fetchAndLoadEmployees().subscribe();
      })
    );
  }

  enableEmployee(id: number): Observable<Employee> {
    return this.http.post<Employee>(`${this.orgApiUrl}/${id}/enable`, {}).pipe(
      tap((updatedEmployee) => {
        this.updateEmployeeInState(updatedEmployee);
      })
    );
  }

  updateBankAccount(id: number, payload: UpdateBankAccountPayload): Observable<Employee> {
    return this.http.put<Employee>(`${this.orgApiUrl}/${id}/bank-account`, payload);
  }

  updateSalaryStructure(id: number, payload: UpdateSalaryPayload): Observable<Employee> {
    return this.http.put<Employee>(`${this.orgApiUrl}/${id}/salary-structure`, payload);
  }

  private updateEmployeeInState(updatedEmployee: Employee) {
    const currentEmployees = this.employeesSubject.getValue();
    const index = currentEmployees.findIndex(emp => emp.id === updatedEmployee.id);
    if (index > -1) {
      const newEmployees = [...currentEmployees];
      newEmployees[index] = updatedEmployee;
      this.employeesSubject.next(newEmployees);
    }
  }
}
