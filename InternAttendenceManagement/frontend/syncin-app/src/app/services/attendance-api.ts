import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class AttendanceApi {
  constructor(private http: HttpClient) {}

  // CR endpoints
  generateToken(): Observable<any> {
    return this.http.post(`${BASE_URL}/cr/generate-token`, {});
  }

  getTodayToken(): Observable<any> {
    return this.http.get(`${BASE_URL}/cr/today-token`);
  }

  getPresentToday(): Observable<any> {
    return this.http.get(`${BASE_URL}/cr/present-today`);
  }

  markDefaulter(attendanceId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/cr/mark-defaulter/${attendanceId}`, {});
  }

  // Intern endpoints
  punchIn(token: string): Observable<any> {
    return this.http.post(`${BASE_URL}/intern/punch-in`, { token });
  }

  getTodayStatus(): Observable<any> {
    return this.http.get(`${BASE_URL}/intern/today-status`);
  }

  submitLeave(data: any): Observable<any> {
    return this.http.post(`${BASE_URL}/intern/leave`, data);
  }

  getMyLeaves(): Observable<any> {
    return this.http.get(`${BASE_URL}/intern/leave`);
  }

  getCalendar(month: string): Observable<any> {
    return this.http.get(`${BASE_URL}/intern/calendar`, { params: { month } });
  }

  getHealth(month: string): Observable<any> {
    return this.http.get(`${BASE_URL}/intern/health`, { params: { month } });
  }

  getCrDailyAnalytics(date?: string): Observable<any> {
    const params: any = {};
    if (date) params.date = date;
    return this.http.get(`${BASE_URL}/cr/daily-analytics`, { params });
  }

  getCrAttendanceSheet(from?: string, to?: string): Observable<any> {
    const params: any = {};
    if (from) params.from = from;
    if (to) params.to = to;
    return this.http.get(`${BASE_URL}/cr/attendance-sheet`, { params });
  }
}
