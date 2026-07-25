package com.locuspark.api.dto.response.report;

import java.math.BigDecimal;

public record RevenueSummaryResponse(
        BigDecimal grossRevenue,
        BigDecimal discountGranted,
        BigDecimal netRevenue,
        BigDecimal averageTicketValue,
        BigDecimal highestTicketValue,
        BigDecimal lowestTicketValue,
        long paidTicketCount,
        long freeExitCount
) {}
