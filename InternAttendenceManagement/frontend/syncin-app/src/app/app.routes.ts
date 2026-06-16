import { Routes } from '@angular/router';
import { AdminSudo } from './components/admin-sudo/admin-sudo';
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard';
import { UserLogin } from './components/user-login/user-login';
import { ChangePassword } from './components/change-password/change-password';
import { Layout } from './components/layout/layout';
import { PocDashboard } from './components/poc-dashboard/poc-dashboard';
import { CrDashboard } from './components/cr-dashboard/cr-dashboard';
import { InternDashboard } from './components/intern-dashboard/intern-dashboard';
import { RoleSelectionComponent } from './components/role-selection/role-selection';

export const routes: Routes = [
  {path: '', component: RoleSelectionComponent},
  { path: 'admin', component: AdminSudo },
  { path: 'admin-dashboard', component: AdminDashboard },
  { path: 'change-password', component: ChangePassword },
  { path: 'login', component: UserLogin },
  {
    path: 'dashboard',
    component: Layout,
    children: [
      { path: '', component: PocDashboard },
      { path: 'cr', component: CrDashboard },
      { path: 'intern', component: InternDashboard }
    ]
  },
  { path: '', component: UserLogin }
];
