import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Vendor, VendorService } from '../../../core/services/vendor.service';
import { PaymentService, VendorPaymentPayload } from '../../../core/services/payment.service';
import { finalize, Observable } from 'rxjs'; // Import Observable
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-create-payment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-payment.component.html',
  styleUrls: ['./create-payment.component.css'] // Corrected
})
export class CreatePaymentComponent implements OnInit {
  paymentForm: FormGroup;
  
  vendors$: Observable<Vendor[]>; // Now use the observable
  isLoadingVendors = true;
  vendorLoadError: string | null = null;

  isSubmitting = false;
  submitError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private vendorService: VendorService,
    private paymentService: PaymentService,
    private notificationService: NotificationService
  ) {
    this.paymentForm = this.fb.group({
      vendorId: [null, Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: ['', [Validators.required, Validators.maxLength(255)]]
    });
    // Assign the observable from the service
    this.vendors$ = this.vendorService.vendors$;
  }

  ngOnInit(): void {
    // Manually trigger the fetch to populate the service's state
    this.vendorService.fetchAndLoadVendors().subscribe({
      next: () => {
        this.isLoadingVendors = false;
      },
      error: (err: any) => { // Added type
        this.vendorLoadError = "Could not load vendors. Please add a vendor first.";
        this.isLoadingVendors = false;
      }
    });
  }

  onSubmit(): void {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.submitError = null;
    const payload: VendorPaymentPayload = this.paymentForm.value;

    this.paymentService.createVendorPayment(payload)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: (response) => {
          this.notificationService.show(response, 'success');
          this.paymentForm.reset();
        },
        error: (err) => {
          this.submitError = err.error?.message || 'Failed to create payment request.';
        }
      });
  }
}