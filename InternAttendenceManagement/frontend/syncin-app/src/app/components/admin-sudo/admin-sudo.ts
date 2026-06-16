// import { Component, signal } from '@angular/core';
// import { FormsModule } from '@angular/forms';
// import { HttpClient } from '@angular/common/http';
// import { Auth } from '../../services/auth';
// import { environment } from '../../../environments/environment';
//
// // Define a strict structure for type-safety and debugging
// interface PointOfContact {
//   empId: string;
//   name: string;
//   email: string;
//   mobileNo?: string;
//   cohorts?: string | string[]; // Can arrive as a raw array or stringified JSON
// }
//
// @Component({
//   selector: 'app-admin-sudo',
//   standalone: true,
//   imports: [FormsModule],
//   templateUrl: './admin-sudo.html',
//   styles: ``,
// })
// export class AdminSudo {
//   // Login
//   loginUsername = '';
//   loginPassword = '';
//   loginError = signal('');
//   isLoggedIn = signal(false);
//
//   // Create POC
//   poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
//   successMsg = signal('');
//   errorMsg = signal('');
//
//   // POC list typed explicitly
//   pocs = signal<PointOfContact[]>([]);
//   pocsErr = signal('');
//   loadingPocs = signal(false);
//
//   constructor(private http: HttpClient, private authService: Auth) {
//     this.isLoggedIn.set(this.authService.isAdminLoggedIn());
//     if (this.isLoggedIn()) {
//       this.loadPocs();
//     }
//   }
//
//   // Helper to attach authorization token safely to requests
//   private getRequestOptions() {
//     const adminToken = this.authService.getAdminToken();
//     return adminToken ? { headers: { Authorization: `Bearer ${adminToken}` } } : {};
//   }
//
//   onLogin(): void {
//     this.loginError.set('');
//     this.authService.adminLogin(this.loginUsername, this.loginPassword).subscribe({
//       next: () => { this.isLoggedIn.set(true); this.loadPocs(); },
//       error: (err: any) => { this.loginError.set(err?.error?.error || 'Login failed.'); }
//     });
//   }
//
//   onCreatePoc(): void {
//     this.successMsg.set('');
//     this.errorMsg.set('');
//
//     // Fixed: Added options parameter so token is attached during creation
//     this.http.post(`${environment.apiUrl}/admin/create-poc`, this.poc, this.getRequestOptions()).subscribe({
//       next: (res: any) => {
//         this.successMsg.set(`POC created: ${res.name} (${res.empId})`);
//         this.poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
//         this.loadPocs();
//       },
//       error: (err: any) => {
//         this.errorMsg.set(err?.error?.error || 'Failed to create POC.');
//       }
//     });
//   }
//
//   loadPocs(): void {
//     this.pocsErr.set('');
//     this.loadingPocs.set(true);
//     const url = `${environment.apiUrl}/admin/pocs`;
//
//     this.http.get<any[]>(url, this.getRequestOptions()).subscribe({
//       next: (rows) => {
//         console.log('Raw API Response payload:', rows);
//
//         // Sanitize response layout: Parse cohorts string safely if backend stores it text-serialized
//         const sanitizedRows: PointOfContact[] = (rows || []).map(p => {
//           let parsedCohorts: string[] = [];
//           if (Array.isArray(p.cohorts)) {
//             parsedCohorts = p.cohorts;
//           } else if (typeof p.cohorts === 'string' && p.cohorts.trim() !== '') {
//             try {
//               parsedCohorts = JSON.parse(p.cohorts);
//             } catch (e) {
//               // Fallback to comma separation if it's stored plain text
//               parsedCohorts = p.cohorts.split(',').map((c: string) => c.trim());
//             }
//           }
//           return { ...p, cohorts: parsedCohorts };
//         });
//
//         this.pocs.set(sanitizedRows);
//       },
//       error: (e: any) => {
//         console.error('admin.pocs error:', e);
//         const status = e?.status;
//         const errBody = e?.error;
//         this.pocsErr.set(`Failed to load POCs: status=${status} body=${JSON.stringify(errBody)}`);
//       },
//       complete: () => this.loadingPocs.set(false)
//     });
//   }
//
//   onLogout(): void {
//     this.authService.adminLogout();
//     this.isLoggedIn.set(false);
//   }
// }


import { Component, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms'; // Imported NgForm module
import { HttpClient } from '@angular/common/http';
import { Auth } from '../../services/auth';
import { environment } from '../../../environments/environment';

// Define a strict structure for type-safety and debugging
interface PointOfContact {
  empId: string;
  name: string;
  email: string;
  mobileNo?: string;
  cohorts?: string | string[]; // Can arrive as a raw array or stringified JSON
}

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

  // POC list typed explicitly
  pocs = signal<PointOfContact[]>([]);
  pocsErr = signal('');
  loadingPocs = signal(false);

  constructor(private http: HttpClient, private authService: Auth) {
    this.isLoggedIn.set(this.authService.isAdminLoggedIn());
    if (this.isLoggedIn()) {
      this.loadPocs();
    }
  }

  // Helper to attach authorization token safely to requests
  private getRequestOptions() {
    const adminToken = this.authService.getAdminToken();
    return adminToken ? { headers: { Authorization: `Bearer ${adminToken}` } } : {};
  }

  onLogin(): void {
    this.loginError.set('');
    this.authService.adminLogin(this.loginUsername, this.loginPassword).subscribe({
      next: () => { this.isLoggedIn.set(true); this.loadPocs(); },
      error: (err: any) => { this.loginError.set(err?.error?.error || 'Login failed.'); }
    });
  }

  // Updated method signature to handle Template Form instance parameters
  onCreatePoc(form: NgForm): void {
    this.successMsg.set('');
    this.errorMsg.set('');

    // Programmatic safety guard to completely intercept invalid submissions
    if (form.invalid) {
      this.errorMsg.set('Please make sure all required fields are correctly completed.');
      return;
    }

    this.http.post(`${environment.apiUrl}/admin/create-poc`, this.poc, this.getRequestOptions()).subscribe({
      next: (res: any) => {
        this.successMsg.set(`POC created: ${res.name} (${res.empId})`);

        // Clear properties and dynamically clear the form's CSS "touched" state indicators
        this.poc = { empId: '', name: '', email: '', mobileNo: '', passwordHash: '' };
        form.resetForm(this.poc);

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

    this.http.get<any[]>(url, this.getRequestOptions()).subscribe({
      next: (rows) => {
        console.log('Raw API Response payload:', rows);

        // Sanitize response layout: Parse cohorts string safely if backend stores it text-serialized
        const sanitizedRows: PointOfContact[] = (rows || []).map(p => {
          let parsedCohorts: string[] = [];
          if (Array.isArray(p.cohorts)) {
            parsedCohorts = p.cohorts;
          } else if (typeof p.cohorts === 'string' && p.cohorts.trim() !== '') {
            try {
              parsedCohorts = JSON.parse(p.cohorts);
            } catch (e) {
              // Fallback to comma separation if it's stored plain text
              parsedCohorts = p.cohorts.split(',').map((c: string) => c.trim());
            }
          }
          return { ...p, cohorts: parsedCohorts };
        });

        this.pocs.set(sanitizedRows);
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
