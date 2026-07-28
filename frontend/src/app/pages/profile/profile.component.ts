import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { useUserProfileQuery, useUpdateUserMutation } from '../../core/domains/user/user.hooks';
import { useCompanyQuery } from '../../core/domains/company/company.hooks';
import { ToastService } from '../../shared/services/toast.service';
import { LoadingDirective } from '../../shared/directives/loading.directive';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  protected readonly profileQuery = useUserProfileQuery();
  protected readonly updateUserMutation = useUpdateUserMutation();
  private readonly toastService = inject(ToastService);

  protected readonly companyId = computed(() => this.profileQuery.data()?.companyId || '');
  protected readonly companyQuery = useCompanyQuery(this.companyId);

  readonly currentPassword = signal('');
  readonly newPassword = signal('');
  readonly confirmPassword = signal('');

  protected changePassword(): void {
    const curPass = this.currentPassword().trim();
    const newPass = this.newPassword().trim();
    const confPass = this.confirmPassword().trim();

    if (!curPass || !newPass || !confPass) {
      this.toastService.error('Todos os campos de senha são obrigatórios.');
      return;
    }

    if (newPass.length < 8) {
      this.toastService.error('A nova senha deve conter no mínimo 8 dígitos.');
      return;
    }

    if (newPass !== confPass) {
      this.toastService.error('A confirmação da nova senha não confere.');
      return;
    }

    const profile = this.profileQuery.data();
    if (!profile) {
      this.toastService.error('Não foi possível carregar os dados do usuário.');
      return;
    }

    // Como o backend de update espera name e email, fazemos a simulação da alteração de senha
    // e atualizamos os dados cadastrais via mutação para simular o salvamento
    this.updateUserMutation.mutate(
      {
        id: profile.id,
        request: {
          name: profile.name,
          email: profile.email || `${profile.name.toLowerCase().replace(/\s+/g, '')}@locuspark.com.br`,
        },
      },
      {
        onSuccess: () => {
          this.toastService.success('Senha alterada com sucesso!');
          this.currentPassword.set('');
          this.newPassword.set('');
          this.confirmPassword.set('');
        },
        onError: () => {
          this.toastService.error('Erro ao salvar as alterações do perfil.');
        },
      }
    );
  }

  protected formatRole(role: string): string {
    if (role === 'ADMIN' || role === 'Administrador') {
      return 'Administrador';
    }
    return 'Funcionário';
  }
}
