import { Component, OnInit } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { Vendor, VendorService } from '../../../core/services/vendor.service';
import { Observable, catchError, of, map } from 'rxjs';
import { AddVendorFormComponent } from '../../../components/add-vendor-form/add-vendor-form.component';

@Component({
  selector: 'app-vendors',
  standalone: true,
  imports: [CommonModule, TitleCasePipe, AddVendorFormComponent],
  templateUrl: './vendors.component.html',
  styleUrls: ['./vendors.component.css'] // Converted to CSS
})
export class VendorsComponent implements OnInit {
  vendors$: Observable<Vendor[]>; // Now a direct observable
  isLoading = true;
  errorMessage: string | null = null;

  isAddVendorModalVisible = false;

  constructor(private vendorService: VendorService) {
    // 1. Assign the observable from the service
    this.vendors$ = this.vendorService.vendors$;
  }

  ngOnInit(): void {
    // 2. Trigger the initial fetch
    this.loadVendors();
  }
  
  // Method now just handles the fetch call and error state
  loadVendors(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.vendorService.fetchAndLoadVendors().pipe(
      catchError(err => {
        this.errorMessage = 'Could not load vendors. Please try again later.';
        return of([]);
      })
    ).subscribe(() => {
      this.isLoading = false;
    });
  }

  openAddVendorModal(): void {
    this.isAddVendorModalVisible = true;
  }

  closeAddVendorModal(): void {
    this.isAddVendorModalVisible = false;
  }

  onVendorAdded(): void {
    this.closeAddVendorModal();
    this.loadVendors();
  }
}