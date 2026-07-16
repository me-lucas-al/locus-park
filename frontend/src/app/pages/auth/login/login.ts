import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { useLoginMutation } from '../../../core/domains/auth/auth.hooks';
import { UserService } from '../../../core/domains/user/user.service';
import { AuthService } from '../../../core/domains/auth/auth.service';
import { ToastService } from '../../../shared/services/toast.service';
import { getBackendErrorMessage } from '../../../core/utils/error-handler.util';
import { lastValueFrom } from 'rxjs';

import { LoadingDirective } from '../../../shared/directives/loading.directive';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoadingDirective],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  usuario = '';
  senha = '';
  senhaVisivel = false;
  erroLogin = '';

  private readonly router = inject(Router);
  protected readonly loginMutation = useLoginMutation();
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  entrar(): void {
    console.log('Login.entrar chamado com usuário:', this.usuario);
    this.erroLogin = '';

    if (!this.usuario.trim() || !this.senha.trim()) {
      this.erroLogin = 'Preencha o usuário e a senha.';
      this.toastService.error(this.erroLogin);
      return;
    }

    this.loginMutation.mutate(
      {
        username: this.usuario,
        password: this.senha,
      },
      {
        onSuccess: async () => {
          try {
            const profile = await lastValueFrom(this.userService.getProfile());
            this.authService.companyId.set(profile.companyId);
            localStorage.setItem('companyId', profile.companyId);
            this.toastService.success('Login realizado com sucesso! Bem-vindo.');
            this.router.navigate(['/dashboard']);
          } catch (err: unknown) {
            this.erroLogin = 'Erro ao carregar os dados de perfil do usuário.';
            this.toastService.error(this.erroLogin);
          }
        },
        onError: (err: any) => {
          console.error('Erro ao realizar login:', err);
          this.erroLogin = getBackendErrorMessage(err, 'Usuário ou senha incorretos.');
          this.toastService.error(this.erroLogin);
        },
      }
    );
  }
}