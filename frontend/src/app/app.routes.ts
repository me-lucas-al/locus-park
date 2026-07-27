import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { Entry } from './pages/entry/entry.component';
import { Exit } from './pages/exit/exit.component';
import { ParkingSpots } from './pages/parking-spots/parking-spots.component';
import { History } from './pages/history/history.component';
import { ManageTeam } from './pages/manage-team/manage-team.component';
import { SettingsPrice } from './pages/settings-price/settings-price.component';
import { Profile } from './pages/profile/profile.component';
import { Login } from './pages/auth/login/login';
import { Register } from './pages/auth/register/register.component';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'cadastro', component: Register },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'entry', component: Entry, canActivate: [authGuard] },
  { path: 'exit', component: Exit, canActivate: [authGuard] },
  { path: 'parking-spots', component: ParkingSpots, canActivate: [authGuard] },
  { path: 'history', component: History, canActivate: [authGuard] },
  {
    path: 'reports',
    loadComponent: () => import('./pages/reports/reports.component').then((m) => m.Reports),
    canActivate: [authGuard, adminGuard],
  },
  { path: 'manage-team', component: ManageTeam, canActivate: [authGuard] },
  { path: 'settings-price', component: SettingsPrice, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];