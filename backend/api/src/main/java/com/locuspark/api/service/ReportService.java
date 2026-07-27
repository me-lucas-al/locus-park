package com.locuspark.api.service;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.*;
import com.locuspark.api.entity.Company;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.ReportCompanyMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.service.report.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final CompanyRepository companyRepository;
    private final TicketWindowLoader ticketWindowLoader;
    private final ReportCompanyMapper reportCompanyMapper;
    private final TicketRowMapper ticketRowMapper;
    private final RevenueSummaryCalculator revenueSummaryCalculator;
    private final StaySummaryCalculator staySummaryCalculator;
    private final OccupancySummaryCalculator occupancySummaryCalculator;
    private final PaymentMethodSummaryCalculator paymentMethodSummaryCalculator;
    private final VehicleTypeSummaryCalculator vehicleTypeSummaryCalculator;
    private final DailySummaryCalculator dailySummaryCalculator;
    private final HourlySummaryCalculator hourlySummaryCalculator;
    private final PartnershipSummaryCalculator partnershipSummaryCalculator;
    private final ClientSummaryCalculator clientSummaryCalculator;

    public ReportResponse getCompanyReport(UUID companyId, ReportFilter filter, ReportDetailLimit detailLimit) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        TicketWindow window = ticketWindowLoader.load(companyId, filter.fromInstant(), filter.toInstant());

        RevenueSummaryResponse revenue = revenueSummaryCalculator.calculate(window);
        StaySummaryResponse stay = staySummaryCalculator.calculate(window);
        OccupancySummaryResponse occupancy = occupancySummaryCalculator.calculate(
                window, company.getTotalSpots(), filter.fromInclusive(), filter.toExclusive());

        List<TicketRowResponse> rows = ticketRowMapper.map(window.all(), detailLimit);
        boolean truncated = detailLimit.exceededBy(window.all().size());

        return new ReportResponse(
                new ReportPeriodResponse(filter.from(), filter.to(), filter.days()),
                reportCompanyMapper.toResponse(company),
                new ReportSummaryResponse(revenue, stay, occupancy),
                paymentMethodSummaryCalculator.calculate(window),
                vehicleTypeSummaryCalculator.calculate(window),
                dailySummaryCalculator.calculate(window, filter.from(), filter.to()),
                hourlySummaryCalculator.calculate(window),
                partnershipSummaryCalculator.calculate(window),
                clientSummaryCalculator.calculate(window),
                rows,
                window.all().size(),
                truncated,
                revenue.netRevenue(),
                revenue.paidTicketCount(),
                stay.averageMinutes());
    }
}
