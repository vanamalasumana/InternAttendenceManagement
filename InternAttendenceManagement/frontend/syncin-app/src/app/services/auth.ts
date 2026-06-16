import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;
const TOKEN_KEY = 'jwt_token';
const ADMIN_TOKEN_KEY = 'admin_token';

@Injectable({ providedIn: 'root' })
export class Auth {
  constructor(private http: HttpClient) {}

  login(empId: string, password: string): Observable<any> {
    // SECURITY FIX: If an admin was logged in, clear their session before a regular user logs in
    this.adminLogout();

    return this.http
      .post<any>(`${BASE_URL}/auth/login`, { empId, password })
      .pipe(tap(res => {
        sessionStorage.setItem(TOKEN_KEY, res.token);
        sessionStorage.setItem('user_role', res.role);
        sessionStorage.setItem('user_id', res.empId);
        sessionStorage.setItem('user_name', res.name || '');
        sessionStorage.setItem('is_first_login', String(res.firstLogin));
      }));
  }

  adminLogin(username: string, password: string): Observable<{ token: string }> {
    // SECURITY FIX: If a standard user was logged in, clear their session before admin logs in
    this.logout();

    return this.http
      .post<{ token: string }>(`${BASE_URL}/admin/login`, { username, password })
      .pipe(tap(res => sessionStorage.setItem(ADMIN_TOKEN_KEY, res.token)));
  }

  getMyProfile(): Observable<{ empId: string; name: string; role: string }> {
    return this.http.get<{ empId: string; name: string; role: string }>(`${BASE_URL}/auth/me`);
  }

  changePassword(newPassword: string): Observable<any> {
    return this.http.put(`${BASE_URL}/auth/change-password`, { newPassword });
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  getAdminToken(): string | null {
    return sessionStorage.getItem(ADMIN_TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdminLoggedIn(): boolean {
    return !!this.getAdminToken();
  }

  isFirstLogin(): boolean {
    return sessionStorage.getItem('is_first_login') === 'true';
  }

  getUserRole(): string | null {
    return sessionStorage.getItem('user_role');
  }

  getUserId(): string | null {
    return sessionStorage.getItem('user_id');
  }

  getUserName(): string | null {
    return sessionStorage.getItem('user_name');
  }

  clearFirstLogin(): void {
    sessionStorage.setItem('is_first_login', 'false');
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem('user_role');
    sessionStorage.removeItem('user_id');
    sessionStorage.removeItem('user_name');
    sessionStorage.removeItem('is_first_login');
  }

  adminLogout(): void {
    sessionStorage.removeItem(ADMIN_TOKEN_KEY);
  }

  // NEW HELPER: Clear absolutely everything (useful for the main entry page)
  clearAllSessions(): void {
    this.logout();
    this.adminLogout();
  }
}
