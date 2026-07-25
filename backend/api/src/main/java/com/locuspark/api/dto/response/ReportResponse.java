package com.locuspark.api.dto.response;

import com.locuspark.api.dto.response.report.*;

import java.math.BigDecimal;
import java.util.List;

public record ReportResponse(
        ReportPeriodResponse period,
        ReportCompanyResponse company,
        ReportSummaryResponse summary,
        List<PaymentMethodSummaryResponse> paymentMethodSummaries,
        List<VehicleTypeSummaryResponse> vehicleTypeSummaries,
        List<DailySummaryResponse> dailySummaries,
        List<HourlySummaryResponse> hourlySummaries,
        List<PartnershipSummaryResponse> partnershipSummaries,
        List<ClientSummaryResponse> clientSummaries,
        List<TicketRowResponse> tickets,
        long ticketCount,
        boolean ticketsTruncated,
        BigDecimal totalRevenue,
        long totalServices,
        double averageStayMinutes
) {}
