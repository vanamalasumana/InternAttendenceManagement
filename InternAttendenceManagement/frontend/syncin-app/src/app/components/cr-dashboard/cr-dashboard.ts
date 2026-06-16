import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { QRCodeComponent } from 'angularx-qrcode';
import { AttendanceApi } from '../../services/attendance-api';

@Component({
  selector: 'app-cr-dashboard',
  standalone: true,
  imports: [QRCodeComponent, FormsModule],
  templateUrl: './cr-dashboard.html',
})
export class CrDashboard {
  token = signal('');
  batchCode = signal('');
  todayDate = signal('');
  msg = signal('');
  err = signal('');

  presentList = signal<any[]>([]);
  defaulterMsg = signal('');
  defaulterErr = signal('');

  analyticsDate = new Date().toISOString().slice(0, 10);
  analyticsRows = signal<any[]>([]);
  analyticsErr = signal('');

  sheetDates = signal<string[]>([]);
  sheetRows = signal<any[]>([]);
  sheetErr = signal('');

  constructor(private api: AttendanceApi) {
    this.loadToken();
    this.loadPresent();
    this.loadDailyAnalytics();
    this.loadAttendanceSheet();
  }

  loadToken() {
    this.api.getTodayToken().subscribe({
      next: (res: any) => {
        this.token.set(res.token || '');
        this.batchCode.set(res.batchCode || '');
        this.todayDate.set(res.date || '');
      }
    });
  }

  generateToken() {
    this.msg.set('');
    this.err.set('');
    this.api.generateToken().subscribe({
      next: (res: any) => {
        this.token.set(res.token);
        this.batchCode.set(res.batchCode);
        this.todayDate.set(res.date);
        this.msg.set(res.message || 'Token generated!');
      },
      error: (e: any) => this.err.set(e?.error?.error || 'Failed to generate token.')
    });
  }

  // CR: mark themselves present using the currently displayed token
  markSelfPresent() {
    this.msg.set('');
    this.err.set('');
    const token = this.token();
    if (!token) {
      this.err.set('No token available. Generate token first.');
      return;
    }

    this.api.punchIn(token).subscribe({
      next: (res: any) => {
        // Show success message and refresh lists/analytics
        this.msg.set(res?.message || 'Marked present successfully.');
        this.loadPresent();
        this.loadDailyAnalytics();
        this.loadAttendanceSheet();
      },
      error: (e: any) => {
        const emsg = e?.error?.error || e?.error?.message || e?.error || 'Failed to mark present.';
        this.err.set(emsg);
      }
    });
  }

  loadPresent() {
    this.api.getPresentToday().subscribe({
      next: (data: any) => this.presentList.set(data),
      error: () => this.presentList.set([])
    });
  }

  markDefaulter(attendanceId: number) {
    this.defaulterMsg.set('');
    this.defaulterErr.set('');
    this.api.markDefaulter(attendanceId).subscribe({
      next: (res: any) => {
        this.defaulterMsg.set(`${res.name} marked as defaulter.`);
        this.loadPresent();
      },
      error: (e: any) => {
        this.defaulterErr.set(e?.error?.error || 'Failed to mark defaulter.');
      }
    });
  }

  loadDailyAnalytics() {
    this.analyticsErr.set('');
    this.api.getCrDailyAnalytics(this.analyticsDate).subscribe({
      next: (res: any) => this.analyticsRows.set(res.rows || []),
      error: (e: any) => {
        this.analyticsErr.set(e?.error?.error || 'Failed to load daily analytics.');
        this.analyticsRows.set([]);
      }
    });
  }

  loadAttendanceSheet() {
    this.sheetErr.set('');
    this.api.getCrAttendanceSheet().subscribe({
      next: (res: any) => {
        this.sheetDates.set(res.dates || []);
        this.sheetRows.set(res.rows || []);
      },
      error: (e: any) => {
        this.sheetErr.set(e?.error?.error || 'Failed to load attendance sheet.');
        this.sheetRows.set([]);
      }
    });
  }
}
