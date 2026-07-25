package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Controle de Acesso de Relatório - ReportAccessControlTest")
class ReportAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ReportService reportService;

    private String authHeaderFor(User user) {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        return "Bearer " + tokenService.generateToken(user);
    }

    @Test
    @DisplayName("EMPLOYEE com empresa deve receber 403 e o serviço nunca é chamado")
    void employeeIsForbidden() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Estacionamento Central").build();
        User employee = User.builder().id(UUID.randomUUID()).username("employee").password("x").role(UserRole.EMPLOYEE).company(company).build();

        mockMvc.perform(get("/reports").header("Authorization", authHeaderFor(employee)))
                .andExpect(status().isForbidden());

        verify(reportService, never()).getCompanyReport(any(), any(), any());
    }

    @Test
    @DisplayName("ADMIN com empresa deve receber 200")
    void adminIsAllowed() throws Exception {
        Company company = Company.builder().id(UUID.randomUUID()).name("Estacionamento Central").build();
        User admin = User.builder().id(UUID.randomUUID()).username("admin").password("x").role(UserRole.ADMIN).company(company).build();

        when(reportService.getCompanyReport(eq(company.getId()), any(ReportFilter.class), eq(ReportDetailLimit.JSON)))
                .thenReturn(mock(ReportResponse.class));

        mockMvc.perform(get("/reports").header("Authorization", authHeaderFor(admin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SUPER_ADMIN sem empresa vinculada deve receber 400")
    void globalSuperAdminWithoutCompanyReturnsBadRequest() throws Exception {
        User superAdmin = User.builder().id(UUID.randomUUID()).username("superadmin").password("x").role(UserRole.SUPER_ADMIN).company(null).build();

        mockMvc.perform(get("/reports").header("Authorization", authHeaderFor(superAdmin)))
                .andExpect(status().isBadRequest());

        verify(reportService, never()).getCompanyReport(any(), any(), any());
    }

    @Test
    @DisplayName("Sem token deve receber 401")
    void noTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }
}
