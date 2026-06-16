import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Auth } from '../../services/auth';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-sudo',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-sudo.html',
  styles: ``,
})
export class AdminSudo {
  // Login
  loginUsername = '';
  loginPassword = '';
  loginError = signal('');
  isLoggedIn = signal(false);

  // Create POC
  poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
  successMsg = signal('');
  errorMsg = signal('');
  // POC list
  pocs = signal<any[]>([]);
  pocsErr = signal('');
  loadingPocs = signal(false);

  constructor(private http: HttpClient, private authService: Auth) {
    this.isLoggedIn.set(this.authService.isAdminLoggedIn());
    if (this.isLoggedIn()) {
      this.loadPocs();
    }
  }

  onLogin(): void {
    this.loginError.set('');
    this.authService.adminLogin(this.loginUsername, this.loginPassword).subscribe({
      next: () => { this.isLoggedIn.set(true); this.loadPocs(); },
      error: (err: any) => { this.loginError.set(err?.error?.error || 'Login failed.'); }
    });
  }

  onCreatePoc(): void {
    this.successMsg.set('');
    this.errorMsg.set('');
     this.http.post(`${environment.apiUrl}/admin/create-poc`, this.poc).subscribe({
      next: (res: any) => {
        this.successMsg.set(`POC created: ${res.name} (${res.empId})`);
        this.poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
        this.loadPocs();
      },
      error: (err: any) => {
        this.errorMsg.set(err?.error?.error || 'Failed to create POC.');
      }
    });
  }

  loadPocs(): void {
    this.pocsErr.set('');
    this.loadingPocs.set(true);
    const url = `${environment.apiUrl}/admin/pocs`;
    const adminToken = this.authService.getAdminToken();
    console.log('Loading POCs from', url, 'adminTokenPresent=', !!adminToken);

    const options = adminToken ? { headers: { Authorization: `Bearer ${adminToken}` } } : {};

    this.http.get<any[]>(url, options).subscribe({
      next: (rows) => {
        console.log('admin.pocs response:', rows);
        this.pocs.set(rows || []);
      },
      error: (e: any) => {
        console.error('admin.pocs error:', e);
        const status = e?.status;
        const errBody = e?.error;
        this.pocsErr.set(`Failed to load POCs: status=${status} body=${JSON.stringify(errBody)}`);
      },
      complete: () => this.loadingPocs.set(false)
    });
  }

  onLogout(): void {
    this.authService.adminLogout();
    this.isLoggedIn.set(false);
  }
}
