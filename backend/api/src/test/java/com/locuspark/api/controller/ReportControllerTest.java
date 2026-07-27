package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import com.locuspark.api.service.ReportService;
import com.locuspark.api.service.report.ReportDetailLimit;
import com.locuspark.api.service.report.export.ReportExportFile;
import com.locuspark.api.service.report.export.ReportExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private ReportExportService reportExportService;

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

    @Test
    @DisplayName("Exportação em PDF deve usar ReportDetailLimit.EXPORT e devolver o content type e o Content-Disposition corretos")
    void exportPdfReturnsCorrectContentTypeAndDisposition() throws Exception {
        assertExportFormat(ReportExportFormat.PDF, "application/pdf", "relatorio.pdf");
    }

    @Test
    @DisplayName("Exportação em XLSX deve devolver o content type correto")
    void exportXlsxReturnsCorrectContentType() throws Exception {
        assertExportFormat(ReportExportFormat.XLSX, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "relatorio.xlsx");
    }

    @Test
    @DisplayName("Exportação em CSV deve devolver o content type correto")
    void exportCsvReturnsCorrectContentType() throws Exception {
        assertExportFormat(ReportExportFormat.CSV, "text/csv", "relatorio.csv");
    }

    private void assertExportFormat(ReportExportFormat format, String contentType, String fileName) throws Exception {
        ReportResponse report = mock(ReportResponse.class);
        when(reportService.getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.EXPORT)))
                .thenReturn(report);
        when(reportExportService.export(eq(report), any(ReportFilter.class), eq(format)))
                .thenReturn(new ReportExportFile(fileName, format, new byte[]{1, 2, 3}));

        mockMvc.perform(get("/reports/export").param("format", format.name()).requestAttr("companyId", companyId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentTypeCompatibleWith(contentType))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\""));

        verify(reportService).getCompanyReport(eq(companyId), any(ReportFilter.class), eq(ReportDetailLimit.EXPORT));
    }
}
