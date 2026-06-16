import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth'; // Double check this path to your auth service

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.isAdminLoggedIn()) {
    return true; // Let them see the admin page
  }

  // Not logged in as Admin? Bounce them back to the main landing page
  router.navigate(['/']);
  return false;
};

export const userGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true; // Let them see the user dashboards
  }

  // Not logged in as User? Bounce them back to the user login page
  router.navigate(['/login']);
  return false;
};
