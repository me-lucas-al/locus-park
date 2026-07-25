package com.locuspark.api.dto.response.report;

import com.locuspark.api.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ClientSummaryResponse(
        UUID clientId,
        String name,
        String cpf,
        long ticketCount,
        BigDecimal totalSpent,
        double averageStayMinutes,
        List<PaymentMethod> paymentMethodsUsed
) {}
