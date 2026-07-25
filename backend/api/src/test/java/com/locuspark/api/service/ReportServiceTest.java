package com.locuspark.api.service;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.*;
import com.locuspark.api.entity.Company;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.ReportCompanyMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.service.report.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Relatório - ReportService")
class ReportServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private TicketWindowLoader ticketWindowLoader;
    @Mock private ReportCompanyMapper reportCompanyMapper;
    @Mock private TicketRowMapper ticketRowMapper;
    @Mock private RevenueSummaryCalculator revenueSummaryCalculator;
    @Mock private StaySummaryCalculator staySummaryCalculator;
    @Mock private OccupancySummaryCalculator occupancySummaryCalculator;
    @Mock private PaymentMethodSummaryCalculator paymentMethodSummaryCalculator;
    @Mock private VehicleTypeSummaryCalculator vehicleTypeSummaryCalculator;
    @Mock private DailySummaryCalculator dailySummaryCalculator;
    @Mock private HourlySummaryCalculator hourlySummaryCalculator;
    @Mock private PartnershipSummaryCalculator partnershipSummaryCalculator;
    @Mock private ClientSummaryCalculator clientSummaryCalculator;

    @InjectMocks
    private ReportService reportService;

    private final UUID companyId = UUID.randomUUID();
    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(companyId).name("Estacionamento Central").totalSpots(50).build();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando a empresa não existir")
    void throwsWhenCompanyNotFound() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        ReportFilter filter = new ReportFilter(LocalDate.now(), LocalDate.now());

        assertThatThrownBy(() -> reportService.getCompanyReport(companyId, filter, ReportDetailLimit.JSON))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve propagar ticketsTruncated quando o limite de detalhe for excedido")
    void propagatesTruncationFlag() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);
        var ticket = com.locuspark.api.service.report.TicketRecordFixture.paid(
                from.atTime(8, 0), from.atTime(9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(ticketWindowLoader.load(eq(companyId), any(), any())).thenReturn(window);
        when(reportCompanyMapper.toResponse(company)).thenReturn(new ReportCompanyResponse(companyId, "Estacionamento Central", "11444777000161", 50));
        when(revenueSummaryCalculator.calculate(window)).thenReturn(new RevenueSummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0));
        when(staySummaryCalculator.calculate(window)).thenReturn(new StaySummaryResponse(0, 0, 0, 0, 0));
        when(occupancySummaryCalculator.calculate(eq(window), eq(50), any(), any())).thenReturn(new OccupancySummaryResponse(50, 0, 0, 0, 0, null, 0, 0, 0));
        when(paymentMethodSummaryCalculator.calculate(window)).thenReturn(List.of());
        when(vehicleTypeSummaryCalculator.calculate(window)).thenReturn(List.of());
        when(dailySummaryCalculator.calculate(eq(window), eq(from), eq(to))).thenReturn(List.of());
        when(hourlySummaryCalculator.calculate(window)).thenReturn(List.of());
        when(partnershipSummaryCalculator.calculate(window)).thenReturn(List.of());
        when(clientSummaryCalculator.calculate(window)).thenReturn(List.of());
        when(ticketRowMapper.map(eq(window.all()), eq(new ReportDetailLimit(0)))).thenReturn(List.of());

        ReportResponse response = reportService.getCompanyReport(companyId, new ReportFilter(from, to), new ReportDetailLimit(0));

        assertThat(response.period().days()).isEqualTo(1);
        assertThat(response.ticketsTruncated()).isTrue();
        assertThat(response.ticketCount()).isEqualTo(1);
    }
}
