package com.locuspark.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locuspark.api.dto.request.RoleUpdateRequest;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    private String validToken;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("testuser", "password", UserRole.EMPLOYEE);
        // Precisamos mockar o repositório porque o SecurityFilter chama findByUsername
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);

        // Geramos um token real usando o serviço real injetado
        validToken = tokenService.generateToken(mockUser);
    }

    @Test
    @DisplayName("Deve retornar 200 OK e perfil ao fornecer um token JWT válido")
    void shouldReturnProfileWithValidToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando o token não é fornecido")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token não fornecido ou cabeçalho Authorization ausente."));
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized com mensagem de erro para token inválido")
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/users/profile")
                        .header("Authorization", "Bearer token-invalido-forjado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token mal formatado, inválido ou expirado."));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando um EMPLOYEE tenta alterar cargo")
    void shouldReturn403WhenEmployeeTriesToUpdateRole() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        RoleUpdateRequest updateRequest = new RoleUpdateRequest(UserRole.ADMIN);

        User targetUser = new User("targetuser", "password", UserRole.EMPLOYEE);
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(patch("/users/" + targetUserId + "/role")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 200 OK quando o ADMIN altera cargo de usuário da mesma empresa")
    void shouldReturn200WhenAdminUpdatesRoleOfUserInSameCompany() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Test Company").build();
        User adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("adminuser")
                .password("password")
                .role(UserRole.ADMIN)
                .company(company)
                .build();
        
        User targetUser = User.builder()
                .id(UUID.randomUUID())
                .username("targetuser")
                .password("password")
                .role(UserRole.EMPLOYEE)
                .company(company)
                .build();

        when(userRepository.findByUsername("adminuser")).thenReturn(adminUser);
        String adminToken = tokenService.generateToken(adminUser);

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        RoleUpdateRequest updateRequest = new RoleUpdateRequest(UserRole.ADMIN);

        mockMvc.perform(patch("/users/" + targetUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }
}