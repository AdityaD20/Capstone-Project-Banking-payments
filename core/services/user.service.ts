import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Interface for the change password payload
export interface ChangePasswordPayload {
  oldPassword: string;
  newPassword: string;
}

// Interface for the user details we get from the backend
export interface UserDetails {
  id: number;
  email: string;
  passwordChangeRequired: boolean;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'https://localhost:8080/api/users';

  constructor(private http: HttpClient) { }

  /**
   * Gets details for the currently authenticated user.
   */
  getMe(): Observable<UserDetails> {
    return this.http.get<UserDetails>(`${this.apiUrl}/me`);
  }

  /**
   * Sends a request to change the current user's password.
   */
  changePassword(payload: ChangePasswordPayload): Observable<string> {
    return this.http.post(`${this.apiUrl}/change-password`, payload, { responseType: 'text' });
  }
}