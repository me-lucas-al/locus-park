import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  useUserProfileQuery,
  useUsersByCompanyQuery,
  useCreateCollaboratorMutation,
  useDeleteUserMutation,
  useUpdateUserRoleMutation
} from '../../core/domains/user/user.hooks';
import { ToastService } from '../../shared/services/toast.service';
import { getApiErrorMessage } from '../../shared/utils/error-message';

import { LoadingDirective } from '../../shared/directives/loading.directive';

@Component({
  selector: 'app-manage-team',
  standalone: true,
  imports: [CommonModule, FormsModule, LoadingDirective],
  templateUrl: './manage-team.html',
  styleUrl: './manage-team.css',
})
export class ManageTeam {
  private readonly toastService = inject(ToastService);

  // Queries
  protected readonly profileQuery = useUserProfileQuery();

  // Signal derivado para obter o companyId do perfil logado
  protected readonly companyId = computed(() => {
    return this.profileQuery.data()?.companyId || '';
  });

  // Query de usuários da empresa
  protected readonly usersQuery = useUsersByCompanyQuery(this.companyId);

  // Mutações
  protected readonly createCollaboratorMutation = useCreateCollaboratorMutation();
  protected readonly deleteUserMutation = useDeleteUserMutation();
  protected readonly updateUserRoleMutation = useUpdateUserRoleMutation();

  protected readonly isCreating = computed(() => 
    this.createCollaboratorMutation.isPending() || this.updateUserRoleMutation.isPending()
  );

  // Estados locais
  readonly searchTerm = signal('');
  readonly modalOpen = signal(false);

  // Form de novo colaborador
  readonly usernameInput = signal('');
  readonly passwordInput = signal('');
  readonly roleInput = signal('COLLABORATOR'); // COLLABORATOR ou ADMIN

  // Filtro de busca na lista de usuários
  protected readonly filteredUsers = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const users = this.usersQuery.data() || [];

    if (!term) return users;

    return users.filter((user) =>
      user.name.toLowerCase().includes(term) ||
      (user.email && user.email.toLowerCase().includes(term))
    );
  });

  protected openModal(): void {
    this.usernameInput.set('');
    this.passwordInput.set('');
    this.roleInput.set('COLLABORATOR');
    this.modalOpen.set(true);
  }

  protected closeModal(): void {
    this.modalOpen.set(false);
  }

  protected addCollaborator(): void {
    const company = this.companyId();
    const username = this.usernameInput().trim();
    const password = this.passwordInput().trim();
    const role = this.roleInput();

    if (!username || !password) {
      this.toastService.error('Nome de usuário e senha são obrigatórios.');
      return;
    }

    if (password.length < 6) {
      this.toastService.error('A senha deve conter pelo menos 6 caracteres.');
      return;
    }

    this.createCollaboratorMutation.mutate(
      {
        companyId: company,
        request: {
          username,
          password,
        },
      },
      {
        onSuccess: (newUser) => {
          // Se o cargo escolhido for ADMIN, fazemos a atualização do cargo do usuário criado
          if (role === 'ADMIN') {
            this.updateUserRoleMutation.mutate(
              {
                id: newUser.id,
                request: { role: 'ADMIN' },
              },
              {
                onSuccess: () => {
                  this.toastService.success(`Colaborador ${username} cadastrado como Administrador!`);
                  this.closeModal();
                },
                onError: () => {
                  this.toastService.success(`Colaborador ${username} cadastrado, mas falhou ao definir perfil de Admin.`);
                  this.closeModal();
                }
              }
            );
          } else {
            this.toastService.success(`Colaborador ${username} cadastrado com sucesso!`);
            this.closeModal();
          }
        },
        onError: (err: unknown) => {
          this.toastService.error(getApiErrorMessage(err, 'Erro ao cadastrar novo colaborador.'));
        }
      }
    );
  }

  protected deleteCollaborator(id: string, name: string): void {
    // Evitar que o usuário exclua a si mesmo
    const currentUser = this.profileQuery.data();
    if (currentUser && currentUser.id === id) {
      this.toastService.error('Você não pode excluir o seu próprio usuário.');
      return;
    }

    if (confirm(`Tem certeza que deseja remover o colaborador @${name}?`)) {
      this.deleteUserMutation.mutate(id, {
        onSuccess: () => {
          this.toastService.success(`Colaborador @${name} removido com sucesso!`);
        },
        onError: (err: unknown) => {
          this.toastService.error(getApiErrorMessage(err, 'Erro ao remover colaborador.'));
        }
      });
    }
  }

  protected formatRole(role: string): string {
    if (role === 'ADMIN' || role === 'Administrador') {
      return 'Administrador';
    }
    return 'Funcionário';
  }
}
