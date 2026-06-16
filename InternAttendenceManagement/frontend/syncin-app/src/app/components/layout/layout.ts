import { Component, signal, OnInit } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Auth } from '../../services/auth';
import { PocApi } from '../../services/poc-api';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './layout.html',
})
export class Layout implements OnInit {
  role = signal('');
  userName = signal('');
  sidebarOpen = signal(true);
  pendingLeaveCount = signal(0);

  constructor(private auth: Auth, private router: Router, private pocApi: PocApi) {
    this.role.set(this.auth.getUserRole() || '');
    this.userName.set(this.auth.getUserName() || '');
  }

  ngOnInit() {
    if (this.role() === 'POC') {
      this.loadPendingLeaves();
    }
  }

  loadPendingLeaves() {
    this.pocApi.getPendingLeaves().subscribe({
      next: (leaves: any[]) => this.pendingLeaveCount.set(leaves.length),
      error: () => {}
    });
  }

  onLogout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  toggleSidebar() {
    this.sidebarOpen.update(v => !v);
  }
}
