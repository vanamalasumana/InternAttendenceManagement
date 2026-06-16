import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-user-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './user-login.html',
})
export class UserLogin {
  empId = '';
  password = '';
  error = signal('');

  constructor(private authService: Auth, private router: Router) {
    if (this.authService.isAdminLoggedIn()) {
      this.router.navigate(['/admin']);
      return;
    }

    // FIX: Only trigger auto-redirect if the user has an active session AND a valid dashboard role.
    if (this.authService.isLoggedIn()) {
      const role = this.authService.getUserRole();
      if (role === 'POC' || role === 'CR' || role === 'INTERN') {
        this.redirectByRole();
      } else {
        // Optional: If the session state is broken/stale, clear it here so it doesn't cause issues
        // this.authService.logout();
      }
    }
  }

  onLogin() {
    this.error.set('');
    this.authService.login(this.empId, this.password).subscribe({
      next: (res: any) => {
        if (res.firstLogin) {
          this.router.navigate(['/change-password']);
        } else {
          this.redirectByRole();
        }
      },
      error: () => {
        this.error.set('Invalid Employee ID or Password.');
      }
    });
  }

  private redirectByRole() {
    const role = this.authService.getUserRole();
    if (role === 'POC') this.router.navigate(['/dashboard']);
    else if (role === 'CR') this.router.navigate(['/dashboard/cr']);
    else if (role === 'INTERN') this.router.navigate(['/dashboard/intern']);
    else this.router.navigate(['/']);
  }
}
