package com.locuspark.api.service;

import com.locuspark.api.dto.request.RegisterRequest;
import com.locuspark.api.dto.request.RoleUpdateRequest;
import com.locuspark.api.dto.request.UserUpdateRequest;
import com.locuspark.api.dto.response.UserResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.exception.UserNotFoundException;
import com.locuspark.api.mapper.UserMapper;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        // 1. Valida se o username já existe
        if (userRepository.findByUsername(request.username()) != null) {
            throw new BusinessException("O usuário '" + request.username() + "' já existe.");
        }

        // 2. Determina a Role (Padrão pública: EMPLOYEE. Se você implementar uma rota restrita para SA, pode vir no request)
        // Como o seu RegisterRequest atual não envia role, assume EMPLOYEE para cadastros comuns.
        UserRole targetRole = UserRole.EMPLOYEE;

        Company company = null;

        // 3. REGRA DE OURO DO MULTI-TENANT:
        // Se NÃO for SUPER_ADMIN, o vínculo com a empresa torna-se estritamente OBRIGATÓRIO no service
        if (targetRole != UserRole.SUPER_ADMIN) {
            if (request.companyId() == null) {
                throw new BusinessException("O ID da empresa é obrigatório.");
            }
            company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new BusinessException("Empresa não encontrada com o ID fornecido."));
        }

        // 4. Converte o DTO para Entidade utilizando o MapStruct
        User user = userMapper.toEntity(request);

        // 5. Aplica as credenciais, papéis e o tenant determinado acima
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(targetRole);
        user.setCompany(company); // Fica nulo se, e somente se, for SUPER_ADMIN

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    // Método alternativo exclusivo para criar o primeiro SUPER_ADMIN do sistema (via Script, Seeder ou Rota Privada)
    @Transactional
    public UserResponse registerSuperAdmin(String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            throw new BusinessException("O usuário '" + username + "' já existe.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.SUPER_ADMIN);
        user.setCompany(null); // Explicitamente sem empresa

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAllByCompany(UUID companyId) {
        return userRepository.findByCompanyId(companyId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        userMapper.updateUserFromDto(request, user);

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateUserRole(UUID targetUserId, RoleUpdateRequest request, User currentUser) {
        // Passo A: Buscar o usuário alvo (targetUserId) no userRepository. Lançar BusinessException se não existir.
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // Passo B: Validar as permissões do currentUser
        UserRole currentRole = currentUser.getRole();

        if (currentRole == UserRole.EMPLOYEE) {
            throw new BusinessException("Acesso negado: Funcionários não podem gerenciar usuários.");
        }

        if (currentRole == UserRole.ADMIN) {
            // O usuário alvo deve pertencer à MESMA empresa do ADMIN
            if (currentUser.getCompany() == null || targetUser.getCompany() == null ||
                    !targetUser.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new BusinessException("Acesso negado: Este usuário pertence a outro pátio.");
            }

            // O ADMIN não pode promover ninguém a SUPER_ADMIN
            if (request.role() == UserRole.SUPER_ADMIN) {
                throw new BusinessException("Acesso negado: Não é permitido promover usuários a SUPER_ADMIN neste escopo.");
            }
        }

        if (currentRole == UserRole.SUPER_ADMIN) {
            // Regra Especial Contextual: se o SUPER_ADMIN possui uma empresa vinculada
            if (currentUser.getCompany() != null) {
                // só altera usuários da sua própria empresa
                if (targetUser.getCompany() == null ||
                        !targetUser.getCompany().getId().equals(currentUser.getCompany().getId())) {
                    throw new BusinessException("Acesso negado: Este usuário pertence a outro pátio.");
                }

                // não pode criar/gerenciar outros SuperAdmins (não pode promover a SUPER_ADMIN)
                if (request.role() == UserRole.SUPER_ADMIN) {
                    throw new BusinessException("Acesso negado: Não é permitido promover usuários a SUPER_ADMIN neste escopo.");
                }
            }
        }

        // Passo C: Atualizar a role do targetUser, salvar no repositório e retornar o UserResponse via mapper.
        targetUser.setRole(request.role());
        User savedUser = userRepository.save(targetUser);
        return userMapper.toResponse(savedUser);
    }
}