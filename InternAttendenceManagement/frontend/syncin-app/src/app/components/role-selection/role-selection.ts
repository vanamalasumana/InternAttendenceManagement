import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [],
  templateUrl: './role-selection.html'
})
export class RoleSelectionComponent {
  constructor(
      private router: Router,
      private auth: Auth
    ) {}

  navigateTo(portal: 'admin' | 'user') {
    if (portal === 'admin') {
      this.auth.logout();
      this.router.navigate(['/admin']);
    } else {
      this.auth.adminLogout();
      this.router.navigate(['/login']);
    }
  }
}
