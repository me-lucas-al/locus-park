package com.locuspark.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locuspark.api.dto.request.AuthRequest;
import com.locuspark.api.dto.request.RegisterRequest;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import com.locuspark.api.service.CompanyService;
import com.locuspark.api.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Nested
    @DisplayName("Fluxo de Login")
    class LoginTests {

        @Test
        @DisplayName("Deve retornar 200 OK e o token no login bem-sucedido")
        void shouldLoginSuccessfully() throws Exception {
            AuthRequest request = new AuthRequest("user", "pass");
            java.util.UUID companyId = java.util.UUID.randomUUID();
            Company mockCompany = new Company();
            mockCompany.setId(companyId);
            User mockUser = new User("user", "hashedPass", UserRole.EMPLOYEE, mockCompany);
            Authentication auth = mock(Authentication.class);

            when(auth.getPrincipal()).thenReturn(mockUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(tokenService.generateToken(mockUser)).thenReturn("fake-jwt-token");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                    .andExpect(jsonPath("$.username").value("user"))
                    .andExpect(jsonPath("$.companyId").value(companyId.toString()));
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized para credenciais inválidas")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            AuthRequest request = new AuthRequest("wrong", "pass");
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Fluxo de Registo")
    class RegisterTests {

        @Test
        @DisplayName("Deve retornar 201 Created quando o registo é bem-sucedido")
        void shouldRegisterSuccessfully() throws Exception {
            java.util.UUID companyId = java.util.UUID.randomUUID();
            RegisterRequest request = new RegisterRequest("newuser", "password123", companyId);
            Company mockCompany = new Company();
            mockCompany.setId(companyId);

            when(userRepository.findByUsername("newuser")).thenReturn(null);
            when(passwordEncoder.encode("password123")).thenReturn("hashedPass");
            when(companyRepository.findById(companyId)).thenReturn(java.util.Optional.of(mockCompany));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict quando o utilizador já existe")
        void shouldReturn409WhenUserExists() throws Exception {
            RegisterRequest request = new RegisterRequest("existinguser", "password123", java.util.UUID.randomUUID());
            when(userRepository.findByUsername("existinguser")).thenReturn(new User());

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }
}