package com.locuspark.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locuspark.api.dto.request.RoleUpdateRequest;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRoleFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    private String getAuthHeader(User user) {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        return "Bearer " + tokenService.generateToken(user);
    }

    @Test
    @DisplayName("EMPLOYEE tentando alterar role -> Deve retornar 403 Forbidden")
    void employeeTriesToUpdateRoleShouldReturnForbidden() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Test Company").build();
        User employee = User.builder()
                .id(UUID.randomUUID())
                .username("employeeUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(employee))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN tentando alterar usuário de outro pátio -> Deve ser bloqueado")
    void adminTriesToUpdateUserOfAnotherCompanyShouldReturnBadRequest() throws Exception {
        Company company1 = Company.builder().id(UUID.randomUUID()).name("Company 1").build();
        Company company2 = Company.builder().id(UUID.randomUUID()).name("Company 2").build();

        User admin = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .password("password")
                .role(UserRole.ADMIN)
                .company(company1)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company2)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Acesso negado: Este usuário pertence a outro pátio."));
    }

    @Test
    @DisplayName("ADMIN tentando promover alguém a SUPER_ADMIN -> Deve ser bloqueado")
    void adminTriesToPromoteToSuperAdminShouldReturnBadRequest() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Company").build();

        User admin = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .password("password")
                .role(UserRole.ADMIN)
                .company(company)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.SUPER_ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Acesso negado: Não é permitido promover usuários a SUPER_ADMIN neste escopo."));
    }

    @Test
    @DisplayName("ADMIN alterando outro ADMIN/EMPLOYEE do mesmo pátio -> Deve retornar 200 OK")
    void adminUpdatesUserInSameCompanyShouldReturnOk() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Company").build();

        User admin = User.builder()
                .id(UUID.randomUUID())
                .username("adminUser")
                .password("password")
                .role(UserRole.ADMIN)
                .company(company)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SUPER_ADMIN com companyId vinculado tentando alterar usuário de outra empresa -> Deve ser bloqueado")
    void superAdminWithCompanyTriesToUpdateUserOfAnotherCompanyShouldReturnBadRequest() throws Exception {
        Company company1 = Company.builder().id(UUID.randomUUID()).name("Company 1").build();
        Company company2 = Company.builder().id(UUID.randomUUID()).name("Company 2").build();

        User superAdmin = User.builder()
                .id(UUID.randomUUID())
                .username("superAdminWithCompany")
                .password("password")
                .role(UserRole.SUPER_ADMIN)
                .company(company1)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company2)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(superAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Acesso negado: Este usuário pertence a outro pátio."));
    }

    @Test
    @DisplayName("SUPER_ADMIN global (companyId null) alterando qualquer usuário -> Deve retornar 200 OK")
    void globalSuperAdminCanUpdateAnyUserShouldReturnOk() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Any Company").build();

        User globalSuperAdmin = User.builder()
                .id(UUID.randomUUID())
                .username("globalSuperAdmin")
                .password("password")
                .role(UserRole.SUPER_ADMIN)
                .company(null)
                .build();

        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetUser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        RoleUpdateRequest request = new RoleUpdateRequest(UserRole.SUPER_ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", getAuthHeader(globalSuperAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
