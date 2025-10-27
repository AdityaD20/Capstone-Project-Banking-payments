import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  message: string;
  type: 'success' | 'danger' | 'info' | 'warning';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private toastSubject = new Subject<ToastMessage>();
  public toastState = this.toastSubject.asObservable();

  show(message: string, type: 'success' | 'danger' | 'info' | 'warning' = 'info') {
    this.toastSubject.next({ message, type });
  }
}