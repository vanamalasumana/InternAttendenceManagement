import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(Auth);
  // Prefer the regular user token for non-admin endpoints, but ensure
  // admin requests use the admin token if available (even when a user
  // token exists). This avoids sending a non-admin token to /admin
  // endpoints which would result in Forbidden/Unauthorized responses.
  const isAdminEndpoint = req.url?.includes('/admin');
  const token = isAdminEndpoint ? (authService.getAdminToken() || authService.getToken()) : (authService.getToken() || authService.getAdminToken());

  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  }

  return next(req);
};


