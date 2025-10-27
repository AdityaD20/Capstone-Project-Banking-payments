import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable, BehaviorSubject, catchError, of, switchMap, take, tap, finalize } from 'rxjs'; // Import finalize
import { AdminService, Organization } from '../../../core/services/admin.service';
import { ConfirmationService } from '../../../core/services/confirmation.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-organizations',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe, DatePipe, CurrencyPipe],
  templateUrl: './organizations.component.html'
})
export class OrganizationsComponent implements OnInit {
  organizations$!: Observable<Organization[]>;
  
  // A simple boolean is easier to manage than a separate observable here.
  isLoading = true;
  errorMessage: string | null = null;
  filterStatus: string = '';

  constructor(
    private adminService: AdminService,
    private confirmationService: ConfirmationService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    // We don't need the refreshTrigger anymore for the initial load.
    this.loadOrganizations();
  }

  // This is now the single point of truth for fetching data.
  loadOrganizations(): void {
    this.isLoading = true;
    this.errorMessage = null;
    
    const statuses = this.filterStatus ? [this.filterStatus] : [];
    
    // The observable is now assigned here directly.
    this.organizations$ = this.adminService.getOrganizations(statuses).pipe(
      finalize(() => this.isLoading = false), // finalize ensures isLoading is set to false whether it succeeds or fails.
      catchError(err => {
        this.errorMessage = "Could not load organizations.";
        return of([]); // Return an empty array to the async pipe so it doesn't break.
      })
    );
  }

  onFilterChange(): void {
    this.loadOrganizations();
  }

  disableOrganization(org: Organization): void {
    this.confirmationService.confirm({
      title: 'Confirm Disable',
      message: `Are you sure you want to disable the organization "${org.name}"? This will prevent them and their employees from logging in.`,
      confirmText: 'Disable',
      isDestructive: true
    }).pipe(take(1)).subscribe(result => {
      if (result) {
        this.adminService.softDeleteOrganization(org.id).subscribe({
          next: () => {
            this.notificationService.show('Organization disabled successfully.', 'success');
            this.loadOrganizations(); // Trigger a refresh after the action
          },
          error: (err) => {
            this.notificationService.show(`Error: ${err.error?.message || 'Could not disable organization.'}`, 'danger');
          }
        });
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'text-bg-success';
      case 'DISABLED': return 'text-bg-secondary';
      case 'REJECTED': return 'text-bg-danger';
      case 'PENDING_INITIAL_APPROVAL':
      case 'PENDING_FINAL_APPROVAL':
        return 'text-bg-warning';
      default: return 'text-bg-info';
    }
  }
}