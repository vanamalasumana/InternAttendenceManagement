import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const BASE_URL = environment.apiUrl + '/poc';

@Injectable({ providedIn: 'root' })
export class PocApi {
  constructor(private http: HttpClient) {}

  getCohorts(): Observable<any> {
    return this.http.get(`${BASE_URL}/cohorts`);
  }

  createCohort(batchCode: string, trackName: string): Observable<any> {
    return this.http.post(`${BASE_URL}/cohorts`, { batchCode, trackName });
  }

  getInterns(batchCode: string): Observable<any> {
    return this.http.get(`${BASE_URL}/cohorts/${batchCode}/interns`);
  }

  onboardIntern(data: any): Observable<any> {
    return this.http.post(`${BASE_URL}/interns`, data);
  }

  promoteIntern(empId: string): Observable<any> {
    return this.http.put(`${BASE_URL}/interns/${empId}/promote`, {});
  }

  demoteIntern(empId: string): Observable<any> {
    return this.http.put(`${BASE_URL}/interns/${empId}/demote`, {});
  }

  getPendingLeaves(): Observable<any> {
    return this.http.get(`${BASE_URL}/leaves/pending`);
  }

  approveLeave(leaveId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/leaves/${leaveId}/approve`, {});
  }

  rejectLeave(leaveId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/leaves/${leaveId}/reject`, {});
  }

  markUlLeave(leaveId: number): Observable<any> {
    return this.http.put(`${BASE_URL}/leaves/${leaveId}/mark-ul`, {});
  }

  getCohortSummary(batchCode: string, range: string, date: string): Observable<any> {
    return this.http.get(`${BASE_URL}/cohorts/${batchCode}/summary`, { params: { range, date } });
  }

  getAttendanceSheet(batchCode: string, from?: string, to?: string): Observable<any> {
    const params: any = {};
    if (from) params.from = from;
    if (to) params.to = to;
    return this.http.get(`${BASE_URL}/cohorts/${batchCode}/attendance-sheet`, { params });
  }
}
