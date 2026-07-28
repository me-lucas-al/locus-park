import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { useRegisterMutation } from '../../../core/domains/auth/auth.hooks';
import { ToastService } from '../../../shared/services/toast.service';
import { getBackendErrorMessage } from '../../../core/utils/error-handler.util';
import { LoadingDirective } from '../../../shared/directives/loading.directive';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LoadingDirective],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  isPasswordVisible = false;
  isConfirmPasswordVisible = false;
  errorMessage = '';

  form = {
    companyName: '',
    cnpj: '',
    totalSpots: null as number | null,
    username: '',
    password: '',
    confirmPassword: '',
  };

  private readonly router = inject(Router);
  protected readonly registerMutation = useRegisterMutation();
  private readonly toastService = inject(ToastService);

  onCnpjInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length > 14) {
      value = value.slice(0, 14);
    }

    if (value.length > 12) {
      value = value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{1,2})$/, '$1.$2.$3/$4-$5');
    } else if (value.length > 8) {
      value = value.replace(/^(\d{2})(\d{3})(\d{3})(\d{1,4})$/, '$1.$2.$3/$4');
    } else if (value.length > 5) {
      value = value.replace(/^(\d{2})(\d{3})(\d{1,3})$/, '$1.$2.$3');
    } else if (value.length > 2) {
      value = value.replace(/^(\d{2})(\d{1,3})$/, '$1.$2');
    }

    this.form.cnpj = value;
    input.value = value;
  }

  private isValidCnpj(cnpj: string): boolean {
    if (cnpj.length !== 14 || /^(\d)\1{13}$/.test(cnpj)) return false;

    let size = cnpj.length - 2;
    let numbers = cnpj.substring(0, size);
    const digits = cnpj.substring(size);
    let sum = 0;
    let pos = size - 7;
    for (let i = size; i >= 1; i--) {
      sum += Number(numbers.charAt(size - i)) * pos--;
      if (pos < 2) pos = 9;
    }
    let result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    if (result !== Number(digits.charAt(0))) return false;

    size = size + 1;
    numbers = cnpj.substring(0, size);
    sum = 0;
    pos = size - 7;
    for (let i = size; i >= 1; i--) {
      sum += Number(numbers.charAt(size - i)) * pos--;
      if (pos < 2) pos = 9;
    }
    result = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    return result === Number(digits.charAt(1));
  }

  onSubmit(): void {
    console.log('Register.onSubmit chamado com form:', this.form);
    this.errorMessage = '';

    const { companyName, cnpj, totalSpots, username, password, confirmPassword } = this.form;

    if (!companyName.trim() || !cnpj.trim() || !totalSpots || !username.trim() || !password || !confirmPassword) {
      this.errorMessage = 'Preencha todos os campos obrigatórios.';
      this.toastService.error(this.errorMessage);
      return;
    }

    const cleanCnpj = cnpj.replace(/\D/g, '');
    if (cleanCnpj.length !== 14 || !this.isValidCnpj(cleanCnpj)) {
      this.errorMessage = 'CNPJ inválido (dígitos verificadores incorretos).';
      this.toastService.error(this.errorMessage);
      return;
    }

    if (password !== confirmPassword) {
      this.errorMessage = 'As senhas não coincidem.';
      this.toastService.error(this.errorMessage);
      return;
    }

    if (password.length < 6) {
      this.errorMessage = 'A senha deve ter pelo menos 6 caracteres.';
      this.toastService.error(this.errorMessage);
      return;
    }

    this.registerMutation.mutate(
      {
        username: username,
        password: password,
        companyName: companyName,
        name: username,
        cnpj: cnpj.replace(/\D/g, ''),
        totalSpots: Number(totalSpots),
      },
      {
        onSuccess: () => {
          this.toastService.success('Cadastro realizado com sucesso!');
          this.router.navigate(['/login']);
        },
        onError: (err: any) => {
          this.errorMessage = getBackendErrorMessage(err, 'Erro ao realizar o cadastro. Verifique as informações.');
          this.toastService.error(this.errorMessage);
        },
      }
    );
  }
}