import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-role-selection',
  standalone: true,
  imports: [],
  templateUrl: './role-selection.html'
})
export class RoleSelectionComponent {
  constructor(private router: Router) {}

  navigateTo(portal: 'admin' | 'user') {
    if (portal === 'admin') {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
