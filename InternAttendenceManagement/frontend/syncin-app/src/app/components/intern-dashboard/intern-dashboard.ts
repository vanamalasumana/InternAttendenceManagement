import { Component, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AttendanceApi } from '../../services/attendance-api';
import { Html5Qrcode } from 'html5-qrcode';

@Component({
  selector: 'app-intern-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './intern-dashboard.html',
})
export class InternDashboard implements OnDestroy {
  status = signal('NOT_MARKED');
  punchedIn = signal(false);
  todayDate = signal('');
  msg = signal('');
  err = signal('');

  // Manual token input
  qrToken = '';

  // Scanner
  scannerActive = signal(false);
  private html5Qrcode: Html5Qrcode | null = null;

  // Leave
  leaveForm = {
    reason: '',
    description: '',
    date: new Date().toISOString().slice(0, 10)
  };
  leaveMsg = signal('');
  leaveErr = signal('');
  leaves = signal<any[]>([]);

  // Calendar
  calendarMonth = new Date().toISOString().slice(0, 7);
  calendarDays = signal<any[]>([]);

  // Health
  healthPct = signal(0);
  healthBucket = signal('HEALTHY');

  constructor(private api: AttendanceApi) {
    this.loadStatus();
    this.loadCalendar();
    this.loadHealth();
    this.loadLeaves();
  }

  loadStatus() {
    this.api.getTodayStatus().subscribe({
      next: (res: any) => {
        this.status.set(res.status);
        this.punchedIn.set(res.punchedIn);
        this.todayDate.set(res.date);
      }
    });
  }

  startScanner() {
    this.err.set('');
    this.msg.set('');
    this.scannerActive.set(true);

    setTimeout(() => {
      this.html5Qrcode = new Html5Qrcode('qr-reader');
      this.html5Qrcode.start(
        { facingMode: 'environment' },
        { fps: 10, qrbox: { width: 200, height: 200 } },
        (decodedText) => {
          this.qrToken = decodedText;
          this.stopScanner();
          this.punchIn();
        },
        () => {}
      ).catch((e: any) => {
        this.err.set('Camera access denied or not available.');
        this.scannerActive.set(false);
      });
    }, 100);
  }

  stopScanner() {
    if (this.html5Qrcode) {
      this.html5Qrcode.stop().then(() => {
        this.html5Qrcode?.clear();
        this.scannerActive.set(false);
      }).catch(() => {
        this.scannerActive.set(false);
      });
    }
  }

  punchIn() {
    this.msg.set('');
    this.err.set('');
    if (!this.qrToken.trim()) {
      this.err.set('Please scan QR or enter the token manually.');
      return;
    }
    this.api.punchIn(this.qrToken.trim()).subscribe({
      next: (res: any) => {
        this.msg.set(res.message);
        this.status.set(res.status);
        this.punchedIn.set(true);
      },
      error: (e: any) => {
        this.err.set(e?.error?.error || 'Punch-in failed.');
      }
    });
  }

  submitLeave() {
    this.leaveMsg.set('');
    this.leaveErr.set('');
    if (!this.leaveForm.reason.trim()) {
      this.leaveErr.set('Reason is required.');
      return;
    }

    this.api.submitLeave(this.leaveForm).subscribe({
      next: () => {
        this.leaveMsg.set('Leave request submitted.');
        this.leaveForm.reason = '';
        this.leaveForm.description = '';
        this.loadLeaves();
      },
      error: (e: any) => {
        this.leaveErr.set(e?.error?.error || 'Failed to submit leave.');
      }
    });
  }

  loadLeaves() {
    this.api.getMyLeaves().subscribe({
      next: (rows: any) => this.leaves.set(rows || []),
      error: () => this.leaves.set([])
    });
  }

  loadCalendar() {
    this.api.getCalendar(this.calendarMonth).subscribe({
      next: (rows: any[]) => {
        const byDate: any = {};
        (rows || []).forEach((r: any) => byDate[r.date] = r.status);
        this.calendarDays.set(this.buildCalendar(this.calendarMonth, byDate));
      },
      error: () => this.calendarDays.set(this.buildCalendar(this.calendarMonth, {}))
    });
  }

  loadHealth() {
    this.api.getHealth(this.calendarMonth).subscribe({
      next: (res: any) => {
        this.healthPct.set(res.attendancePercentage || 0);
        this.healthBucket.set(res.bucket || 'HEALTHY');
      },
      error: () => {
        this.healthPct.set(0);
        this.healthBucket.set('HEALTHY');
      }
    });
  }

  onMonthChange() {
    this.loadCalendar();
    this.loadHealth();
  }

  getHealthBarClass() {
    const p = this.healthPct();
    if (p < 85) return 'bg-red-500';
    if (p <= 87) return 'bg-orange-500';
    return 'bg-green-500';
  }

  getDayClass(status: string) {
    if (status === 'BLANK') return 'bg-transparent border-transparent text-transparent pointer-events-none';

    // Modern muted style for weekend holidays
    if (status === 'WEEKEND') return 'bg-slate-100 text-slate-400 border-slate-200 opacity-80';

    if (status === 'PRESENT') return 'bg-green-100 text-green-700 border-green-300';
    if (status === 'AL') return 'bg-blue-100 text-blue-700 border-blue-300';
    if (status === 'ABSENT' || status === 'UL') return 'bg-red-100 text-red-700 border-red-300';
    return 'bg-gray-50 text-gray-500 border-gray-200';
  }

  private buildCalendar(month: string, byDate: any) {
    const [yearText, monthText] = month.split('-');
    const year = Number(yearText);
    const m = Number(monthText);
    const daysInMonth = new Date(year, m, 0).getDate();
    const rows: any[] = [];

    const firstWeekday = new Date(year, m - 1, 1).getDay();

    // leading blanks
    for (let i = 0; i < firstWeekday; i++) {
      rows.push({ date: `blank-start-${i}`, day: '', status: 'BLANK' });
    }

    // actual days
    for (let day = 1; day <= daysInMonth; day++) {
      const date = `${yearText}-${monthText}-${String(day).padStart(2, '0')}`;
      const currentDayOfWeek = new Date(year, m - 1, day).getDay();

      // If Saturday (6) or Sunday (0), retain the day number but set status to 'WEEKEND'
      if (currentDayOfWeek === 0 || currentDayOfWeek === 6) {
        rows.push({ date, day, status: 'WEEKEND' });
      } else {
        rows.push({ date, day, status: byDate[date] || 'NOT_MARKED' });
      }
    }

    // trailing blanks
    while (rows.length % 7 !== 0) {
      const t = rows.length;
      rows.push({ date: `blank-end-${t}`, day: '', status: 'BLANK' });
    }

    return rows;
  }

  ngOnDestroy() {
    this.stopScanner();
  }
}
