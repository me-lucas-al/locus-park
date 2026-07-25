package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes de Controlador de Relatório - ReportController")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private final UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("Sem datas deve aplicar o filtro padrão de 30 dias")
    void noDatesUsesDefaultFilter() throws Exception {
        when(reportService.getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.JSON)))
                .thenReturn(mock(ReportResponse.class));

        mockMvc.perform(get("/reports").requestAttr("companyId", companyId))
                .andExpect(status().isOk());

        verify(reportService).getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.JSON));
    }

    @Test
    @DisplayName("from invalido deve retornar 400")
    void invalidFromReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/reports").param("from", "lixo").requestAttr("companyId", companyId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    @DisplayName("Sem companyId deve retornar 400")
    void missingCompanyIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    @DisplayName("companyId enviado por query string deve ser ignorado")
    void companyIdQueryParamIsIgnored() throws Exception {
        UUID otherCompanyId = UUID.randomUUID();
        when(reportService.getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.JSON)))
                .thenReturn(mock(ReportResponse.class));

        mockMvc.perform(get("/reports").param("companyId", otherCompanyId.toString()).requestAttr("companyId", companyId))
                .andExpect(status().isOk());

        verify(reportService).getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.JSON));
    }
}
