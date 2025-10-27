import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.currentUserValue) {
    return true; // User is logged in, allow access
  }
  // User is not logged in, redirect them to an appropriate login page
  router.navigate(['/']); // Redirect to home page is safest
  return false;
};

// // src/app/core/guards/auth.guard.ts
// import { inject } from '@angular/core';
// import { CanActivateFn, Router } from '@angular/router';
// import { AuthService } from '../services/auth.service';

// export const authGuard: CanActivateFn = (route, state) => {
//   // Modern Angular guards use functional style with inject()
//   const authService = inject(AuthService);
//   const router = inject(Router);

//   // Check if a user is currently logged in
//   if (authService.currentUserValue) {
//     // Optional: Check for specific roles if needed in the future
//     // const { roles } = route.data;
//     // if (roles && !roles.some((r: string) => authService.currentUserValue?.roles.includes(r))) {
//     //   router.navigate(['/']); // Or an unauthorized page
//     //   return false;
//     // }
//     return true; // User is logged in, allow access
//   }

//   // User is not logged in, redirect them to the login page
//   // We will create the specific login pages later
//   router.navigate(['/organization/auth']);
//   return false; // Block access
// };
