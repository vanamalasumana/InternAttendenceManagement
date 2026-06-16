import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PocApi } from '../../services/poc-api';

@Component({
  selector: 'app-poc-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './poc-dashboard.html',
})
export class PocDashboard {
  // Cohorts
  cohorts = signal<any[]>([]);
  newCohort = { batchCode: '', trackName: '' };
  cohortMsg = signal('');
  cohortErr = signal('');

  // Selected cohort interns
  selectedCohort = signal<any>(null);
  interns = signal<any[]>([]);

  // Onboard modal
  showModal = signal(false);
  internForm = { empId: '', name: '', email: '', mobileNo: '', batchCode: '' };
  onboardMsg = signal('');
  onboardErr = signal('');
  tempPassword = signal('');

  // Promote
  promoteMsg = signal('');
  promoteErr = signal('');

  // Leave inbox
  pendingLeaves = signal<any[]>([]);
  leaveMsg = signal('');
  leaveErr = signal('');

  // Summary + attendance sheet
  summaryPreset: 'today' | 'this_week' | 'this_month' = 'today';
  summaryRows = signal<any[]>([]);
  summaryMeta = signal<any>(null);
  summaryErr = signal('');

  sheetDates = signal<string[]>([]);
  sheetRows = signal<any[]>([]);
  sheetErr = signal('');

  constructor(private api: PocApi) {
    this.loadCohorts();
    this.loadPendingLeaves();
  }

  loadCohorts() {
    this.api.getCohorts().subscribe({
      next: (data: any) => this.cohorts.set(data),
      error: () => {}
    });
  }

  createCohort() {
    this.cohortMsg.set('');
    this.cohortErr.set('');
    if (!this.newCohort.batchCode.trim() || !this.newCohort.trackName.trim()) {
      this.cohortErr.set('Batch Code and Track Name are required.');
      return;
    }
    this.api.createCohort(this.newCohort.batchCode, this.newCohort.trackName).subscribe({
      next: (res: any) => {
        this.cohortMsg.set(`Cohort "${res.batchCode}" created!`);
        this.newCohort = { batchCode: '', trackName: '' };
        this.loadCohorts();
      },
      error: (err: any) => {
        this.cohortErr.set(err?.error?.error || 'Failed to create cohort.');
      }
    });
  }

  selectCohort(cohort: any) {
    this.selectedCohort.set(cohort);
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.getInterns(cohort.batchCode).subscribe({
      next: (data: any) => this.interns.set(data),
      error: () => this.interns.set([])
    });

    this.generateSummary();
    this.loadAttendanceSheet();
  }

  openOnboardModal() {
    this.internForm = { empId: '', name: '', email: '', mobileNo: '', batchCode: this.selectedCohort()?.batchCode };
    this.onboardMsg.set('');
    this.onboardErr.set('');
    this.tempPassword.set('');
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  onboardIntern() {
    this.onboardMsg.set('');
    this.onboardErr.set('');
    this.api.onboardIntern(this.internForm).subscribe({
      next: (res: any) => {
        this.onboardMsg.set(`Intern "${res.name}" onboarded!`);
        this.tempPassword.set(res.tempPassword);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.onboardErr.set(err?.error?.error || 'Failed to onboard intern.');
      }
    });
  }

  promote(empId: string) {
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.promoteIntern(empId).subscribe({
      next: (res: any) => {
        this.promoteMsg.set(`${res.name} promoted to CR!`);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.promoteErr.set(err?.error?.error || 'Failed to promote.');
      }
    });
  }

  demote(empId: string) {
    this.promoteMsg.set('');
    this.promoteErr.set('');
    this.api.demoteIntern(empId).subscribe({
      next: (res: any) => {
        this.promoteMsg.set(`${res.name} demoted to INTERN.`);
        this.selectCohort(this.selectedCohort());
      },
      error: (err: any) => {
        this.promoteErr.set(err?.error?.error || 'Failed to demote.');
      }
    });
  }

  // Leave inbox
  loadPendingLeaves() {
    this.api.getPendingLeaves().subscribe({
      next: (rows: any) => this.pendingLeaves.set(rows || []),
      error: () => this.pendingLeaves.set([])
    });
  }

  approveLeave(item: any) {
    this.leaveMsg.set('');
    this.leaveErr.set('');
    this.api.approveLeave(item.id).subscribe({
      next: () => {
        this.leaveMsg.set(`Approved leave for ${item.internName || item.internEmpId}.`);
        this.loadPendingLeaves();
        this.loadAttendanceSheet();
      },
      error: (e: any) => this.leaveErr.set(e?.error?.error || 'Failed to approve leave.')
    });
  }

  rejectLeave(item: any) {
    this.leaveMsg.set('');
    this.leaveErr.set('');
    this.api.rejectLeave(item.id).subscribe({
      next: () => {
        this.leaveMsg.set(`Rejected leave for ${item.internName || item.internEmpId}.`);
        this.loadPendingLeaves();
      },
      error: (e: any) => this.leaveErr.set(e?.error?.error || 'Failed to reject leave.')
    });
  }

  markUl(item: any) {
    this.leaveMsg.set('');
    this.leaveErr.set('');
    this.api.markUlLeave(item.id).subscribe({
      next: () => {
        this.leaveMsg.set(`Marked UL for ${item.internName || item.internEmpId}.`);
        this.loadPendingLeaves();
        this.loadAttendanceSheet();
      },
      error: (e: any) => this.leaveErr.set(e?.error?.error || 'Failed to mark UL.')
    });
  }

  // Summary
  generateSummary() {
    const cohort = this.selectedCohort();
    if (!cohort) return;
    this.summaryErr.set('');
    const apiRange = this.summaryPreset === 'this_week' ? 'weekly'
                   : this.summaryPreset === 'this_month' ? 'monthly'
                   : 'daily';
    const todayKey = this.localTodayKey();
    this.api.getCohortSummary(cohort.batchCode, apiRange, todayKey).subscribe({
      next: (res: any) => {
        this.summaryRows.set(res.rows || []);
        this.summaryMeta.set(res);
      },
      error: (e: any) => {
        this.summaryErr.set(e?.error?.error || 'Failed to generate summary.');
        this.summaryRows.set([]);
        this.summaryMeta.set(null);
      }
    });
  }

  onPresetChange() {
    if (this.selectedCohort()) this.generateSummary();
  }

  summaryLabel() {
    if (this.summaryPreset === 'this_week') return 'This Week';
    if (this.summaryPreset === 'this_month') return 'This Month';
    return 'Today';
  }

  localTodayKey() {
    const n = new Date();
    return `${n.getFullYear()}-${String(n.getMonth()+1).padStart(2,'0')}-${String(n.getDate()).padStart(2,'0')}`;
  }

  // Attendance sheet
  loadAttendanceSheet() {
    const cohort = this.selectedCohort();
    if (!cohort) return;
    this.sheetErr.set('');
    this.api.getAttendanceSheet(cohort.batchCode).subscribe({
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

  getHealthClass(bucket: string) {
    if (bucket === 'CRITICAL') return 'text-red-600 bg-red-50';
    if (bucket === 'AT_RISK') return 'text-orange-600 bg-orange-50';
    return 'text-green-700 bg-green-50';
  }
}
