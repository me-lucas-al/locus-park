package com.locuspark.api.service;

import com.locuspark.api.dto.request.RoleUpdateRequest;
import com.locuspark.api.dto.response.UserResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.exception.UserNotFoundException;
import com.locuspark.api.mapper.UserMapper;
import com.locuspark.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Company company1;
    private Company company2;

    @BeforeEach
    void setUp() {
        company1 = Company.builder().id(UUID.randomUUID()).name("Company 1").build();
        company2 = Company.builder().id(UUID.randomUUID()).name("Company 2").build();
    }

    @Nested
    @DisplayName("Role Update Security Rules")
    class RoleUpdateSecurityTests {

        @Test
        @DisplayName("Deve atualizar o cargo com sucesso quando o currentUser é SUPER_ADMIN")
        void shouldUpdateRoleWhenCurrentUserIsSuperAdmin() {
            User superAdmin = User.builder().id(UUID.randomUUID()).role(UserRole.SUPER_ADMIN).build();
            User targetUser = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company1).build();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse(targetUser.getId(), targetUser.getUsername(), UserRole.ADMIN, company1.getId()));

            UserResponse response = userService.updateUserRole(targetUser.getId(), request, superAdmin);

            assertNotNull(response);
            assertEquals(UserRole.ADMIN, targetUser.getRole());
            verify(userRepository).save(targetUser);
        }

        @Test
        @DisplayName("Deve atualizar o cargo com sucesso quando o currentUser é ADMIN da mesma empresa")
        void shouldUpdateRoleWhenCurrentUserIsAdminOfSameCompany() {
            User admin = User.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).company(company1).build();
            User targetUser = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company1).build();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(new UserResponse(targetUser.getId(), targetUser.getUsername(), UserRole.ADMIN, company1.getId()));

            UserResponse response = userService.updateUserRole(targetUser.getId(), request, admin);

            assertNotNull(response);
            assertEquals(UserRole.ADMIN, targetUser.getRole());
            verify(userRepository).save(targetUser);
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar atualizar cargo se currentUser for EMPLOYEE")
        void shouldThrowExceptionWhenCurrentUserIsEmployee() {
            User employee = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company1).build();
            User targetUser = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company1).build();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    userService.updateUserRole(targetUser.getId(), request, employee)
            );

            assertEquals("Acesso negado: Funcionários não podem gerenciar usuários.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro quando o ADMIN tenta atualizar cargo de usuário de outra empresa")
        void shouldThrowExceptionWhenAdminTriesToUpdateUserOfAnotherCompany() {
            User admin = User.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).company(company1).build();
            User targetUser = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company2).build();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    userService.updateUserRole(targetUser.getId(), request, admin)
            );

            assertEquals("Acesso negado: Este usuário pertence a outro pátio.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro quando o ADMIN tenta promover usuário a SUPER_ADMIN")
        void shouldThrowExceptionWhenAdminTriesToPromoteToSuperAdmin() {
            User admin = User.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).company(company1).build();
            User targetUser = User.builder().id(UUID.randomUUID()).role(UserRole.EMPLOYEE).company(company1).build();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.SUPER_ADMIN);

            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    userService.updateUserRole(targetUser.getId(), request, admin)
            );

            assertEquals("Acesso negado: Não é permitido promover usuários a SUPER_ADMIN neste escopo.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar erro quando o usuário alvo não existe")
        void shouldThrowExceptionWhenTargetUserNotFound() {
            User superAdmin = User.builder().id(UUID.randomUUID()).role(UserRole.SUPER_ADMIN).build();
            UUID nonExistentId = UUID.randomUUID();
            RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

            when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    userService.updateUserRole(nonExistentId, request, superAdmin)
            );

            assertEquals("Usuário não encontrado.", exception.getMessage());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("List All Users By Company")
    class ListAllByCompanyTests {

        @Test
        @DisplayName("Deve retornar lista de usuários da empresa correspondente")
        void shouldReturnUsersOfCompany() {
            UUID companyId = UUID.randomUUID();
            Company targetCompany = Company.builder().id(companyId).name("Target Company").build();
            User user1 = User.builder().id(UUID.randomUUID()).username("user1").role(UserRole.EMPLOYEE).company(targetCompany).build();
            User user2 = User.builder().id(UUID.randomUUID()).username("user2").role(UserRole.EMPLOYEE).company(targetCompany).build();
            
            java.util.List<User> mockUsers = java.util.List.of(user1, user2);
            when(userRepository.findByCompanyId(companyId)).thenReturn(mockUsers);
            when(userMapper.toResponse(user1)).thenReturn(new UserResponse(user1.getId(), "user1", UserRole.EMPLOYEE, companyId));
            when(userMapper.toResponse(user2)).thenReturn(new UserResponse(user2.getId(), "user2", UserRole.EMPLOYEE, companyId));

            java.util.List<UserResponse> responses = userService.listAllByCompany(companyId);

            assertNotNull(responses);
            assertEquals(2, responses.size());
            verify(userRepository).findByCompanyId(companyId);
            verify(userMapper).toResponse(user1);
            verify(userMapper).toResponse(user2);
        }
    }
}
