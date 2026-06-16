import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboard {
  pocs = signal<any[]>([]);
  err = signal('');
  loading = signal(false);

  constructor(private http: HttpClient) {
    this.loadPocs();
  }

  loadPocs() {
    this.err.set('');
    this.loading.set(true);
    this.http.get<any[]>(`${environment.apiUrl}/admin/pocs`).subscribe({
      next: (rows) => this.pocs.set(rows || []),
      error: (e: any) => this.err.set(e?.error?.error || 'Failed to load POCs'),
      complete: () => this.loading.set(false)
    });
  }
}

