import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { DocUploaderComponent } from '../../../components/doc-uploader/doc-uploader.component';
import { OrganizationService } from '../../../core/services/organization.service';
import { Organization } from '../../../core/services/admin.service';
import { catchError, of, forkJoin, map, Observable } from 'rxjs';
import { EmployeeService } from '../../../core/services/employee.service';
import { VendorService } from '../../../core/services/vendor.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-organization-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DocUploaderComponent,
    CurrencyPipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class OrganizationDashboardComponent implements OnInit {
  isLoading = true;
  errorMessage: string | null = null;

  organization$: Observable<Organization | null>;
  activeEmployeeCount$!: Observable<number>;
  vendorCount$!: Observable<number>;
  pendingPaymentCount$!: Observable<number>;

  constructor(
    private orgService: OrganizationService,
    private employeeService: EmployeeService,
    private vendorService: VendorService,
    private notificationService: NotificationService
  ) {
    this.organization$ = this.orgService.organization$;
    this.activeEmployeeCount$ = this.employeeService.employees$.pipe(
      map(employees => employees.filter(e => e.status === 'ACTIVE').length)
    );
    this.vendorCount$ = this.vendorService.vendors$.pipe(
      map(vendors => vendors.length)
    );
    this.pendingPaymentCount$ = this.orgService.paymentHistory$.pipe(
      map(payments => payments.filter(p => p.status === 'PENDING').length)
    );
  }

  ngOnInit(): void {
    // This method is now renamed for clarity
    this.loadInitialDashboardState();
  }

  loadInitialDashboardState(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // STEP 1: Always fetch the organization's own details first.
    this.orgService.fetchAndLoadMe().subscribe({
      next: (org) => {
        // STEP 2: Check the organization's status.
        if (org && org.status === 'ACTIVE') {
          // STEP 3: If active, fetch the rest of the data for the summary cards.
          this.loadActiveDashboardDetails();
        } else {
          // If not active, we are done. The HTML's ngSwitch will handle the display.
          this.isLoading = false;
        }
      },
      error: (err) => {
        this.errorMessage = "Could not load your organization details. Please try refreshing the page.";
        this.isLoading = false;
      }
    });
  }

  // This method is now only called when we know the organization is active.
  loadActiveDashboardDetails(): void {
    forkJoin([
      this.employeeService.fetchAndLoadEmployees(),
      this.vendorService.fetchAndLoadVendors(),
      this.orgService.fetchAndLoadPaymentHistory()
    ]).pipe(
      catchError(err => {
        // This is a secondary error handler in case one of these fails
        this.errorMessage = "Could not load all summary card data.";
        return of(null);
      })
    ).subscribe(() => {
      this.isLoading = false;
    });
  }

  refreshOrgDetails(): void {
    this.isLoading = true;
    this.orgService.fetchAndLoadMe().subscribe({
      next: () => {
        this.isLoading = false;
        this.notificationService.show('Organization status has been updated.', 'success');
      },
      error: () => this.isLoading = false
    });
  }
}