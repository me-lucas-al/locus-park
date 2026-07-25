package com.locuspark.api.dto.response.report;

import com.locuspark.api.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodSummaryResponse(PaymentMethod method, long ticketCount, BigDecimal revenue, double sharePercent) {}
