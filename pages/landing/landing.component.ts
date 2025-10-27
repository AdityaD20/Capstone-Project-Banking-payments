import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service'; // 1. Import

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css'] // Converted to CSS
})
export class LandingComponent {
  constructor(
    private router: Router,
    private notificationService: NotificationService // 2. Inject
    ) {}

  navigateTo(route: string): void {
    if (route.includes('organization')) {
      this.router.navigate([route]);
    } else if (route.includes('admin') || route.includes('employee')) {
      this.router.navigate(['/login']);
    } else {
      this.notificationService.show('Invalid route!', 'danger');
    }
  }
}