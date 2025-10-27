import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ToastComponent } from './components/toast/toast.component';
import { ConfirmationDialogComponent } from './components/confirmation-dialog/confirmation-dialog.component'; // 1. Import

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterModule,
    ToastComponent,
    ConfirmationDialogComponent // 2. Add to imports
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'payment-payroll-frontend';
}