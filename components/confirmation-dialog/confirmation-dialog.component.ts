import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { Modal } from 'bootstrap';
import { ConfirmationConfig, ConfirmationService } from '../../core/services/confirmation.service';
@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-dialog.component.html',
})
export class ConfirmationDialogComponent implements OnInit, OnDestroy {
  @ViewChild('confirmationModal') modalElement!: ElementRef;
  private modalInstance: Modal | null = null;
  private subscription!: Subscription;
  config: ConfirmationConfig | null = null;
  constructor(private confirmationService: ConfirmationService) {}
  ngOnInit(): void {
    this.subscription = this.confirmationService.requestState.subscribe((config) => {
      this.config = config;
      this.getModal().show();
    });
  }
  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
  private getModal(): Modal {
    if (!this.modalInstance) {
      this.modalInstance = new Modal(this.modalElement.nativeElement, {
        keyboard: false, // Prevent ESC key from closing, so we can control the response
        backdrop: 'static', // Prevent clicking outside from closing
      });
    }
    return this.modalInstance;
  }
  onConfirm(): void {
    this.getModal().hide();
    this.confirmationService.respond(true);
  }
  onCancel(): void {
    this.getModal().hide();
    this.confirmationService.respond(false);
  }
}
