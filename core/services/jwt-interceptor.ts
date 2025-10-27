// src/app/core/services/jwt.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  // Inject the AuthService to get the token
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Check if a token exists
  if (token) {
    // If a token exists, clone the request and add the Authorization header
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    // Pass the cloned request to the next handler in the chain
    return next(clonedRequest);
  }

  // If no token exists, pass the original request along without modification
  return next(req);
};