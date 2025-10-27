import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { OrganizationService } from '../../core/services/organization.service';
import { Organization } from '../../core/services/admin.service';
import { Observable } from 'rxjs'; // Import Observable

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'] 
})
export class DashboardLayoutComponent implements OnInit {
  organization$: Observable<Organization | null>; // Now an observable
  isLoadingOrgStatus = true; // Flag to wait for the status check

  constructor(
    public authService: AuthService,
    private orgService: OrganizationService
  ) {
    // Assign the observable from the service
    this.organization$ = this.orgService.organization$;
  }

  ngOnInit(): void {
    // If the logged-in user is an organization, trigger the fetch.
    if (this.userHasRole('ROLE_ORGANIZATION')) {
      // Use the new reactive method
      this.orgService.fetchAndLoadMe().subscribe({
        next: () => {
          this.isLoadingOrgStatus = false;
        },
        error: () => {
          this.isLoadingOrgStatus = false;
        }
      });
    } else {
      this.isLoadingOrgStatus = false;
    }
  }

  userHasRole(role: string): boolean {
    return this.authService.currentUserValue?.roles.includes(role) ?? false;
  }

  logout(): void {
    this.authService.logout();
  }
}