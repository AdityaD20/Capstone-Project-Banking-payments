import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface ConfirmationConfig {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  isDestructive?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmationService {
  private requestSubject = new Subject<ConfirmationConfig>();
  private resultSubject = new Subject<boolean>();

  public requestState = this.requestSubject.asObservable();

  /**
   * Opens a confirmation modal.
   * @param config The title, message, and button text for the modal.
   * @returns An Observable that emits `true` if confirmed, or `false` if cancelled.
   */
  confirm(config: ConfirmationConfig): Observable<boolean> {
    // Push the configuration to the component to open the modal
    this.requestSubject.next(config);
    // Return an observable that the calling component can subscribe to for the result
    return this.resultSubject.asObservable();
  }

  /**
   * Called by the confirmation component to send the result back.
   * @param result The user's choice (true for confirm, false for cancel).
   */
  respond(result: boolean): void {
    this.resultSubject.next(result);
  }
}